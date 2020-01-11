.class public server_get_num_of_online_users
.super Message

.field private receiver Lserver;
.field private sender LActor;

.method public <init>(Lserver;LActor;)V
.limit stack 16
.limit locals 3
aload_0
invokespecial Message/<init>()V
aload_0
aload_1
putfield server_get_num_of_online_users/receiver Lserver;
aload_0
aload_2
putfield server_get_num_of_online_users/sender LActor;
return
.end method

.method public execute()V
.limit stack 16
.limit locals 3
aload_0
getfield server_get_num_of_online_users/receiver Lserver;
aload_0
getfield server_get_num_of_online_users/sender LActor;
invokevirtual server/get_num_of_online_users(LActor;)V
return
.end method
