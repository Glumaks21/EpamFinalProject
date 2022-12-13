package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.Data;
import lombok.ToString;
import ua.maksym.hlushchenko.dao.entity.*;
import ua.maksym.hlushchenko.dao.entity.role.Reader;

import java.util.*;

@Data
@ToString(exclude = {"receipts", "subscriptions"})
public class ReaderImpl extends UserImpl implements Reader {
    private boolean blocked;

    private List<Receipt> receipts = new ArrayList<>();
    private List<Subscription> subscriptions = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reader)) return false;
        Reader reader = (Reader) o;
        return super.equals(o) && isBlocked() == reader.isBlocked();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isBlocked(), getReceipts(), getSubscriptions());
    }
}
