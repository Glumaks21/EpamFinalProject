package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.Data;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class ReaderImpl extends UserImpl implements Reader {
    private boolean isBlocked;

    List<Receipt> receipts = new ArrayList<>();
    List<Subscription> subscriptions = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reader)) return false;
        Reader reader = (Reader) o;
        return isBlocked() == reader.isBlocked() &&
                Objects.equals(getReceipts(), reader.getReceipts()) &&
                Objects.equals(getSubscriptions(), reader.getSubscriptions());
    }
}
