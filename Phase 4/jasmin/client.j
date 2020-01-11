.class public client
.super Actor

.field server Lserver;
.field friend Lclient;
.field name Ljava/lang/String;
.field msg I;

.method public <init>(I)V
.limit stack 2
.limit locals 2
aload_0
iload_1
invokespecial Actor/<init>(I)V
return
.end method

.method public initial(Ljava/lang/String)V
.limit stack 16
.limit locals 3
putfield client/name string
aload_2
putfield client/msg int
ldc 0
getstatic java/lang/System/out Ljava/io/PrintStream;
aload_2
invokevirtual java/io/PrintStream/println(Ljava/lang/String)V
aload_0
getfield client/server Lserver;
aload_0
aload_2
invokevirtual server/send_connect(LActor;Ljava/lang/String;)V
FOR2:
aload_0
getfield client/server Lserver;
aload_0
invokevirtual server/send_get_num_of_online_users(LActor;)V
aload_0
getfield client/friend Lclient;
aload_0
invokevirtual friend/send_im_online(LActor;)V
aload_0
getfield client/msg I;
ldc 120
if_icmpne NOT_EQUAL4
iconst_1
goto EQUAL_COMPLETE5
NOT_EQUAL4:iconst_0
EQUAL_COMPLETE5:
ifeq ELSE6
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "offline!"
invokevirtual java/io/PrintStream/println(Ljava/lang/String)V
goto END_FOR3
goto IF_COMPLETE7
ELSE6:IF_COMPLETE7:
goto FOR2
END_FOR3:
return
.end method


.method public setKnownActors(Lserver;Lclient;)V
.limit stack 16
.limit locals 3
aload_0
aload_1
putfield client/server Lserver;
aload_0
aload_2
putfield client/friend Lclient;
return
.end method

.method public send_im_online(LActor;)V
.limit stack 16
.limit locals 2
aload_0
new client_im_online
dup
aload_0
aload_1
invokespecial client_im_online/<init>(Lclient;LActor;)V
invokevirtual client/send(LMessage;)V
return
.end method

.method public im_online(LActor;)V
.limit stack 16
.limit locals 2
getstatic java/lang/System/out Ljava/io/PrintStream;
ldc "online friend!"
invokevirtual java/io/PrintStream/println(Ljava/lang/String)V
aload_0
aload_0
aload_0
getfield client/msg I;
ldc 1
iadd
putfield client/msg I
return
.end method

