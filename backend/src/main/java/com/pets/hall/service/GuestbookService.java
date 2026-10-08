package com.pets.hall.service;

import com.pets.hall.mapper.GuestbookMapper;
import com.pets.hall.model.AgreeCount;
import com.pets.hall.model.GuestbookEntry;
import com.pets.hall.model.SessionUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class GuestbookService {
    static final int PAGE = 20;
    private final GuestbookMapper guestbookMapper;

    public GuestbookService(GuestbookMapper guestbookMapper) {
        this.guestbookMapper = guestbookMapper;
    }

    public Map<String, Object> latest(@Nullable SessionUser viewer, @Nullable Long beforeId) {
        return page(guestbookMapper.findLatest(beforeId, PAGE + 1), viewer);
    }

    public Map<String, Object> mine(@Nullable SessionUser account, @Nullable Long beforeId) {
        if (account == null) {
            return page(List.of(), null);
        }
        return page(guestbookMapper.findMine(account.getId(), beforeId, PAGE + 1), account);
    }

    public String post(@Nullable SessionUser account, @Nullable String content, @Nullable Long parentId) {
        if (account == null) {
            return "登录后才能留言。";
        }
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            return "写一句再送出。";
        }
        GuestbookEntry entry = new GuestbookEntry();
        entry.setUserId(account.getId());
        entry.setUsername(account.getUsername());
        entry.setContent(text);
        if (parentId != null) {
            GuestbookEntry target = guestbookMapper.findById(parentId);
            if (target == null) {
                return "这句话不能再往下回。";
            }
            if (text.length() > 200) {
                return "回一句请控制在 200 字以内。";
            }
            if (target.getParentId() == null) {
                entry.setParentId(target.getId());
            } else {
                entry.setParentId(target.getParentId());
                entry.setReplyTo(target.getUsername());
            }
        } else if (text.length() > 500) {
            return "留言请控制在 500 字以内。";
        }
        guestbookMapper.insert(entry);
        return "";
    }

    public String update(@Nullable SessionUser account, long id, @Nullable String content) {
        if (account == null) {
            return "登录后才能修改自己的留言。";
        }
        GuestbookEntry entry = guestbookMapper.findById(id);
        if (entry == null) {
            return "这句话找不到了。";
        }
        if (!account.getId().equals(entry.getUserId())) {
            return "只能修改自己的留言。";
        }
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            return "写一句再送出。";
        }
        if (entry.getParentId() == null) {
            if (text.length() > 500) {
                return "留言请控制在 500 字以内。";
            }
        } else if (text.length() > 200) {
            return "回一句请控制在 200 字以内。";
        }
        guestbookMapper.updateContent(id, text);
        return "";
    }

    public String remove(@Nullable SessionUser account, long id) {
        if (account == null) {
            return "登录后才能删除自己的留言。";
        }
        GuestbookEntry entry = guestbookMapper.findById(id);
        if (entry == null) {
            return "这句话找不到了。";
        }
        if (!account.getId().equals(entry.getUserId())) {
            return "只能删除自己的留言。";
        }
        List<Long> ids = new ArrayList<>();
        ids.add(entry.getId());
        if (entry.getParentId() == null) {
            for (GuestbookEntry reply : guestbookMapper.findReplies(List.of(entry.getId()))) {
                ids.add(reply.getId());
            }
            guestbookMapper.deleteAgrees(ids);
            guestbookMapper.deleteReplies(entry.getId());
        } else {
            guestbookMapper.deleteAgrees(ids);
        }
        guestbookMapper.deleteById(entry.getId());
        return "";
    }

    public String toggleAgree(@Nullable SessionUser account, long messageId) {
        if (account == null) {
            return "登录后才能点赞。";
        }
        GuestbookEntry note = guestbookMapper.findById(messageId);
        if (note == null) {
            return "这句话找不到了。";
        }
        if (guestbookMapper.countAgreed(messageId, account.getId()) > 0) {
            guestbookMapper.deleteAgree(messageId, account.getId());
        } else {
            guestbookMapper.insertAgree(messageId, account.getId());
        }
        return "";
    }

    private Map<String, Object> page(List<GuestbookEntry> rows, @Nullable SessionUser viewer) {
        boolean more = rows.size() > PAGE;
        List<GuestbookEntry> shown = more ? rows.subList(0, PAGE) : rows;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("notes", compose(shown, viewer));
        body.put("more", more);
        return body;
    }

    private List<Map<String, Object>> compose(List<GuestbookEntry> notes, @Nullable SessionUser viewer) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (notes.isEmpty()) {
            return list;
        }
        List<Long> ids = new ArrayList<>();
        for (GuestbookEntry note : notes) {
            ids.add(note.getId());
        }
        Map<Long, List<GuestbookEntry>> replies = new HashMap<>();
        List<Long> counted = new ArrayList<>(ids);
        for (GuestbookEntry reply : guestbookMapper.findReplies(ids)) {
            replies.computeIfAbsent(reply.getParentId(), key -> new ArrayList<>()).add(reply);
            counted.add(reply.getId());
        }
        Map<Long, Long> counts = new HashMap<>();
        for (AgreeCount row : guestbookMapper.countAgrees(counted)) {
            counts.put(row.getMessageId(), row.getTotal());
        }
        Set<Long> agreed = new HashSet<>();
        if (viewer != null) {
            agreed.addAll(guestbookMapper.findAgreedIds(viewer.getId(), counted));
        }
        for (GuestbookEntry note : notes) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", note.getId());
            item.put("username", note.getUsername());
            item.put("content", note.getContent());
            item.put("createdAt", note.getCreatedAt());
            item.put("agreeCount", counts.getOrDefault(note.getId(), 0L));
            item.put("agreed", agreed.contains(note.getId()));
            item.put("owned", owns(viewer, note));
            List<Map<String, Object>> children = new ArrayList<>();
            for (GuestbookEntry reply : replies.getOrDefault(note.getId(), List.of())) {
                Map<String, Object> child = new LinkedHashMap<>();
                child.put("id", reply.getId());
                child.put("username", reply.getUsername());
                child.put("content", reply.getContent());
                child.put("createdAt", reply.getCreatedAt());
                if (reply.getReplyTo() != null && !reply.getReplyTo().isBlank()) {
                    child.put("replyTo", reply.getReplyTo());
                }
                child.put("agreeCount", counts.getOrDefault(reply.getId(), 0L));
                child.put("agreed", agreed.contains(reply.getId()));
                child.put("owned", owns(viewer, reply));
                children.add(child);
            }
            item.put("replies", children);
            list.add(item);
        }
        return list;
    }

    private static boolean owns(@Nullable SessionUser viewer, GuestbookEntry entry) {
        return viewer != null && viewer.getId() != null && viewer.getId().equals(entry.getUserId());
    }
}
