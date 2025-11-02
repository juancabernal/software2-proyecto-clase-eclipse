package co.edu.uco.ucochallenge.domain.user.model;

import co.edu.uco.ucochallenge.crosscuting.helper.ObjectHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.TextHelper;
import co.edu.uco.ucochallenge.crosscuting.helper.UUIDHelper;
import co.edu.uco.ucochallenge.domain.pagination.PageCriteria;

public record UserSearchQuery(UserFilter filter, PageCriteria pagination) {

    private static final UserFilter DEFAULT_FILTER = new UserFilter(
            UUIDHelper.getDefault(),
            TextHelper.getDefault(),
            TextHelper.getDefault(),
            TextHelper.getDefault(),
            UUIDHelper.getDefault(),
            TextHelper.getDefault(),
            TextHelper.getDefault());

    private static final PageCriteria DEFAULT_PAGINATION = PageCriteria.of(0, 20);

    public UserSearchQuery {
        filter = ObjectHelper.getDefault(filter, DEFAULT_FILTER);
        pagination = ObjectHelper.getDefault(pagination, DEFAULT_PAGINATION);
    }
}
