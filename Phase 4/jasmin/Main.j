.class public Main
.super java/lang/Object

.method public <init>()V
.limit stack 1
.limit locals 1
0: aload_0
1: invokespecial java/lang/Object/<init>()V
4: return
.end method

.method public static main([Ljava/lang/String;)V
.limit stack 16
.limit locals 4
new server
dup
iconst_10
invokespecial server/<init>(I)V
astore_1
new client
dup
iconst_3
invokespecial client/<init>(I)V
astore_2
new client
dup
iconst_3
invokespecial client/<init>(I)V
astore_3
aload_1
invokevirtual server/setKnownActors()V
aload_2
aload_1
aload_3
invokevirtual client/setKnownActors(Lserver;Lclient;)V
aload_3
aload_1
aload_2
invokevirtual client/setKnownActors(Lserver;Lclient;)V
aload_1
invokevirtual server/initial(Ljava/lang/String;I)V
aload_2
invokevirtual client/initial(Ljava/lang/String)V
aload_3
invokevirtual client/initial(Ljava/lang/String)V
aload_1
invokevirtual server/start()V
aload_2
invokevirtual client/start()V
aload_3
invokevirtual client/start()V
return
.end method
