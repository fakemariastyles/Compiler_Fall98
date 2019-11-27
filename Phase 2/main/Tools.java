class Tools {
    static boolean codeIsValid = true;

    static public boolean putActor(ActorDeclaration actor) {
        boolean error = false;
        try {
            SymbolTable.top.put(new SymbolTableActorItem(actor));
        } catch (ItemAlreadyExistsException e) {
            //TODO
        }
        return error;
    }
}