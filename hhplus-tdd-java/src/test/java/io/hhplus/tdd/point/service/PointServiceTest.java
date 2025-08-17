package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.exception.NotEnoughPointException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PointServiceTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointTable = mock(UserPointTable.class);
        pointHistoryTable = mock(PointHistoryTable.class);
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    @Test
    @DisplayName("포인트 충전 테스트 - Mocking Tables")
    void charge_createsBalanceAndHistory_withMocks() {
        long userId = 1L;
        long amount = 100L;

        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));
        when(userPointTable.insertOrUpdate(eq(userId), eq(amount))) 
                .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        UserPoint result = pointService.charge(userId, amount);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals(amount, result.point());

        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, amount);
        verify(pointHistoryTable).insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
        verifyNoMoreInteractions(pointHistoryTable, userPointTable);
    }

    @Test
    @DisplayName("포인트 사용(잔액 차감, 히스토리 생성) - Mocking Tables")
    void use_afterCharge_reducesBalanceAndRecordsHistory_withMocks() {
        long userId = 2L;
        long initial = 200L;
        long useAmount = 50L;

        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, initial, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(eq(userId), eq(initial - useAmount)))
                .thenReturn(new UserPoint(userId, initial - useAmount, System.currentTimeMillis()));

        UserPoint afterUse = pointService.use(userId, useAmount);

        assertEquals(initial - useAmount, afterUse.point());

        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, initial - useAmount);

        ArgumentCaptor<Long> tsCaptor = ArgumentCaptor.forClass(Long.class);
        verify(pointHistoryTable).insert(eq(userId), eq(useAmount), eq(TransactionType.USE), tsCaptor.capture());
        assertNotNull(tsCaptor.getValue());
        verifyNoMoreInteractions(pointHistoryTable, userPointTable);
    }

    @Test
    @DisplayName("포인트 부족 예외처리 - Mocking Tables")
    void use_insufficient_throws_noSideEffects_withMocks() {
        long userId = 3L;
        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));

        NotEnoughPointException ex = assertThrows(NotEnoughPointException.class,
                () -> pointService.use(userId, 10L));
        assertTrue(ex.getMessage().toLowerCase().contains("not enough"));

        verify(userPointTable).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
        verifyNoMoreInteractions(pointHistoryTable, userPointTable);
    }
}