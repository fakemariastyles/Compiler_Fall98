.class public server
.super Actor

.field addr Ljava/lang/String;
.field num_of_users I;
.field total_reqs I;
.field is_serve I;

.method public <init>(I)V
.limit stack 2
.limit locals 2
aload_0
iload_1
invokespecial Actor/<init>(I)V
return
.end method

.method public initial(Ljava/lang/String;I)V
.limit stack 16
.limit locals 4
putfield server/addr string
aload_3
putfield server/is_serve boolean
iload_3
aload_0
aload_0
ldc 0
putfield server/total_reqs I
aload_0
aload_0
ldc 0
putfield server/num_of_users I
getstatic java/lang/System/out Ljava/io/PrintStream;
aload_3
invokevirtual java/io/PrintStream/println(Ljava/lang/String)V
return
.end method


.method public setKnownActors()V
.limit stack 16
.limit locals 1
return
.end method

.method public send_connect(LActor;Ljava/lang/String)V
.limit stack 16
.limit locals 3
aload_0
new server_connect
dup
aload_0
aload_1
aload_2
invokespecial server_connect/<init>(Lserver;LActor;Ljava/lang/String)V
invokevirtual server/send(LMessage;)V
return
.end method

.method public connect(LActor;Ljava/lang/String)V
.limit stack 16
.limit locals 3
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "connected:"
invokevirtual java/io/PrintStream/println(Ljava/lang/String)V
getstatic java/lang/System/out Ljava/io/PrintStream;
aload_2
invokevirtual java/io/PrintStream/println(Ljava/lang/String)V
aload_0
aload_0
aload_0
getfield server/num_of_users I;
ldc 1
iadd
putfield server/num_of_users I
return
.end method

.method public send_get_num_of_online_users(LActor;)V
.limit stack 16
.limit locals 2
aload_0
new server_get_num_of_online_users
dup
aload_0
aload_1
invokespecial server_get_num_of_online_users/<init>(Lserver;LActor;)V
invokevirtual server/send(LMessage;)V
return
.end method

.method public get_num_of_online_users(LActor;)V
.limit stack 16
.limit locals 2
aload_0
getfield server/is_serve I;
ifeq ELSE0
aload_0
aload_0
aload_0
getfield server/total_reqs I;
ldc 1
iadd
putfield server/total_reqs I
getstatic java/lang/System/out Ljava/io/PrintStream;
aload_0
getfield server/addr Ljava/lang/String;
invokevirtual java/io/PrintStream/println(Ljava/lang/String)V
getstatic java/lang/System/out Ljava/io/PrintStream;
aload_0
getfield server/num_of_users I;
invokevirtual java/io/PrintStream/println(I)V
goto IF_COMPLETE1
ELSE0:IF_COMPLETE1:
return
.end method

