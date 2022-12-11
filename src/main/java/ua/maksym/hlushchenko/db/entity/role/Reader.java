package ua.maksym.hlushchenko.db.entity.role;

import ua.maksym.hlushchenko.db.entity.Receipt;
import ua.maksym.hlushchenko.db.entity.Subscription;

import java.util.List;
import java.util.Set;

public interface Reader extends User {
    boolean isBlocked();
    void setBlocked(boolean state);
    List<Receipt> getReceipts();
    void setReceipts(List<Receipt> receipts);
    List<Subscription> getSubscriptions();
    void setSubscriptions(List<Subscription> subscriptions);
}
