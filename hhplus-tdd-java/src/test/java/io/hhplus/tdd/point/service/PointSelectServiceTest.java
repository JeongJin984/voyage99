package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PointSelectServiceTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointSelectService pointSelectService;

    @BeforeEach
    void setUp() {
        userPointTable = mock(UserPointTable.class);
        pointHistoryTable = mock(PointHistoryTable.class);
        pointSelectService = new PointSelectService(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("getPoint는 UserPointTable.selectById의 반환값을 그대로 반환한다")
    void getPoint_returns_value_from_userPointTable() {
        long userId = 10L;
        UserPoint expected = new UserPoint(userId, 300L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(expected);

        UserPoint actual = pointSelectService.getPoint(userId);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(userPointTable).selectById(eq(userId));
        verifyNoMoreInteractions(userPointTable);
        verifyNoInteractions(pointHistoryTable);
    }

    @Test
    @DisplayName("getPointHistory는 PointHistoryTable.selectAllByUserId의 반환값을 그대로 반환한다")
    void getPointHistory_returns_list_from_pointHistoryTable() {
        long userId = 20L;
        long now = System.currentTimeMillis();
        List<PointHistory> expected = List.of(
                new PointHistory(1L, userId, 100L, TransactionType.CHARGE, now - 1000),
                new PointHistory(2L, userId, 40L, TransactionType.USE, now - 500)
        );
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expected);

        List<PointHistory> actual = pointSelectService.getPointHistory(userId);

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
        verify(pointHistoryTable).selectAllByUserId(eq(userId));
        verifyNoMoreInteractions(pointHistoryTable);
        verifyNoInteractions(userPointTable);
    }
}
