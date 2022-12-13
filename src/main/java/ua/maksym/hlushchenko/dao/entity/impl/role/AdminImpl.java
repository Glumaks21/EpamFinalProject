package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ua.maksym.hlushchenko.dao.entity.role.Admin;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminImpl extends UserImpl implements Admin {}
