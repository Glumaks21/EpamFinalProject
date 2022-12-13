package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ua.maksym.hlushchenko.dao.entity.role.Librarian;

@Data
@EqualsAndHashCode(callSuper = true)
public class LibrarianImpl extends UserImpl implements Librarian {}
