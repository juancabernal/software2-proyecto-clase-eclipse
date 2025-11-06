package co.edu.uco.ucochallenge.application.user.search.dto;

import java.util.List;

public class UserSearchQueryResponseDTO {

        private List<UserSearchQuerySummaryDTO> users;
        private int page;
        private int size;
        private long totalElements;

        public List<UserSearchQuerySummaryDTO> getUsers() {
                return users;
        }

        public void setUsers(final List<UserSearchQuerySummaryDTO> users) {
                this.users = users;
        }

        public int getPage() {
                return page;
        }

        public void setPage(final int page) {
                this.page = page;
        }

        public int getSize() {
                return size;
        }

        public void setSize(final int size) {
                this.size = size;
        }

        public long getTotalElements() {
                return totalElements;
        }

        public void setTotalElements(final long totalElements) {
                this.totalElements = totalElements;
        }
}
