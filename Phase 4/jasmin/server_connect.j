.class public server_connect
.super Message

.field private name Ljava/lang/String
.field private receiver Lserver;
.field private sender LActor;

.method public <init>(Lserver;LActor;Ljava/lang/String)V
.limit stack 16
.limit locals 4
aload_0
invokespecial Message/<init>()V
aload_0
aload_1
putfield server_connect/receiver Lserver;
aload_0
aload_2
putfield server_connect/sender LActor;
aload_0
aload_3
putfield server_connect/name Ljava/lang/String
return
.end method

.method public execute()V
.limit stack 16
.limit locals 4
aload_0
getfield server_connect/receiver Lserver;
aload_0
getfield server_connect/sender LActor;
aload_0
getfield server_connect/name Ljava/lang/String
invokevirtual server/connect(LActor;Ljava/lang/String)V
return
.end method
