class eee {
    private static void registerMethods(ServerRPC server) {
        DB.delete(Student.class, UUID.fromString(id))
                 .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete student: " + error.getMessage())));
    }
}
