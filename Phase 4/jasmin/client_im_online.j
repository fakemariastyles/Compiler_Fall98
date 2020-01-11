.class public client_im_online
.super Message

.field private receiver Lclient;
.field private sender LActor;

.method public <init>(Lclient;LActor;)V
.limit stack 16
.limit locals 3
aload_0
invokespecial Message/<init>()V
aload_0
aload_1
putfield client_im_online/receiver Lclient;
aload_0
aload_2
putfield client_im_online/sender LActor;
return
.end method

.method public execute()V
.limit stack 16
.limit locals 3
aload_0
getfield client_im_online/receiver Lclient;
aload_0
getfield client_im_online/sender LActor;
invokevirtual client/im_online(LActor;)V
return
.end method
