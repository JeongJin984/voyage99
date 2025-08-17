package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.exception.NotEnoughPointException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint charge(long userId, long amount) {
        UserPoint userPoint = userPointTable.selectById(userId);
        userPoint = userPointTable.insertOrUpdate(userId, userPoint.point() + amount);
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return userPoint;
    }

    public UserPoint use(long userId, long amount) {
        UserPoint userPoint = userPointTable.selectById(userId);
        if(userPoint.point() < amount) throw new NotEnoughPointException("Not enough point");
        userPoint = userPointTable.insertOrUpdate(userId, userPoint.point() - amount);
        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
        return userPoint;
    }
}
