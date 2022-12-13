package ua.maksym.hlushchenko.dao.entity.role;

import ua.maksym.hlushchenko.dao.entity.Receipt;
import ua.maksym.hlushchenko.dao.entity.Subscription;

import java.util.List;

public interface Reader extends User {
    boolean isBlocked();
    void setBlocked(boolean state);
    List<Receipt> getReceipts();
    void setReceipts(List<Receipt> receipts);
    List<Subscription> getSubscriptions();
    void setSubscriptions(List<Subscription> subscriptions);
}
