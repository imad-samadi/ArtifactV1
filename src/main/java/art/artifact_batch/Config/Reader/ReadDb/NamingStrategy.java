package art.artifact_batch.Config.Reader.ReadDb;

public interface NamingStrategy {
    NamingStrategy DEFAULT = new DefaultNamingStrategy();

    String toTableName(String className);
    String toColumnName(String propertyName);

    class DefaultNamingStrategy implements NamingStrategy {
        @Override
        public String toTableName(String className) {
            return camelToSnake(className); // Example: UserProfile -> user_profile
        }

        @Override
        public String toColumnName(String propertyName) {
            return camelToSnake(propertyName); // Example: userName -> user_name
        }

        private String camelToSnake(String str) {
            if (str == null || str.isEmpty()) return "";
            return str.replaceAll("([a-z\\d])([A-Z])", "$1_$2").toLowerCase();
        }
    }
}
