package com.pets.hall.mapper;

import com.pets.hall.model.AgreeCount;
import com.pets.hall.model.GuestbookEntry;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GuestbookMapper {
    List<GuestbookEntry> findLatest(@Param("beforeId") Long beforeId, @Param("limit") int limit);

    List<GuestbookEntry> findMine(
            @Param("userId") long userId, @Param("beforeId") Long beforeId, @Param("limit") int limit);

    List<GuestbookEntry> findReplies(@Param("ids") List<Long> ids);

    GuestbookEntry findById(@Param("id") long id);

    int insert(GuestbookEntry entry);

    int updateContent(@Param("id") long id, @Param("content") String content);

    int deleteById(@Param("id") long id);

    int deleteReplies(@Param("parentId") long parentId);

    int deleteAgrees(@Param("ids") List<Long> ids);

    List<AgreeCount> countAgrees(@Param("ids") List<Long> ids);

    int countAgreed(@Param("messageId") long messageId, @Param("userId") long userId);

    List<Long> findAgreedIds(@Param("userId") long userId, @Param("ids") List<Long> ids);

    int insertAgree(@Param("messageId") long messageId, @Param("userId") long userId);

    int deleteAgree(@Param("messageId") long messageId, @Param("userId") long userId);
}
