package org.example.botproject;

public class LoginResponse {
        private String token;
        private String error;
        private String product;
        private String status;

        // Getters and setters
        public String getToken() {
            return token;
        }

        public String getError() {
            return error;
        }

        public String getProduct() {
            return product;
        }

        public String getStatus() {
            return status;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public void setError(String error) {
            this.error = error;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public void setStatus(String status) {
            this.status = status;
        }


}
