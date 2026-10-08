package com.pets.hall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pets.hall.mapper.GuestbookMapper;
import com.pets.hall.model.GuestbookEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GuestbookServiceTest {
    @Mock
    private GuestbookMapper guestbookMapper;

    @InjectMocks
    private GuestbookService guestbookService;

    @Test
    void firstPageSaysWhenOlderNotesRemain() {
        when(guestbookMapper.findLatest(isNull(), eq(GuestbookService.PAGE + 1))).thenReturn(entries(21));
        when(guestbookMapper.findReplies(anyList())).thenReturn(List.of());
        when(guestbookMapper.countAgrees(anyList())).thenReturn(List.of());

        Map<String, Object> page = guestbookService.latest(null, null);

        assertTrue((Boolean) page.get("more"));
        assertEquals(20, ((List<?>) page.get("notes")).size());
    }

    @Test
    void shortPageDoesNotAskForReplies() {
        when(guestbookMapper.findLatest(isNull(), eq(GuestbookService.PAGE + 1))).thenReturn(List.of());

        Map<String, Object> page = guestbookService.latest(null, null);

        assertFalse((Boolean) page.get("more"));
        assertTrue(((List<?>) page.get("notes")).isEmpty());
        verify(guestbookMapper, never()).findReplies(anyList());
    }

    private static List<GuestbookEntry> entries(int count) {
        List<GuestbookEntry> rows = new ArrayList<>();
        for (int i = count; i >= 1; i--) {
            GuestbookEntry entry = new GuestbookEntry();
            entry.setId((long) i);
            entry.setUserId(1L);
            entry.setUsername("厅客");
            entry.setContent("一句");
            entry.setCreatedAt("2026-10-08 12:00");
            rows.add(entry);
        }
        return rows;
    }
}
