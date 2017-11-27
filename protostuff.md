### protostuff

```   
    序列化
    Schema<String> schema = RuntimeSchema.getSchema(String.class);
    byte[] keyBytes = ProtostuffIOUtil.toByteArray("123", schema, LinkedBuffer.allocate(1024));
    byte[] valueBytes = ProtostuffIOUtil.toByteArray("value", schema, LinkedBuffer.allocate(1024));
    resource.append(keyBytes, valueBytes);
    反序列化
    byte[] bytes = resource.get(keyBytes);
    String s = new String();
    ProtostuffIOUtil.mergeFrom(bytes,s,schema);
    System.out.println(s);
```
 