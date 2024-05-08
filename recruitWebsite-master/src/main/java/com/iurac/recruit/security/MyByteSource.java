package com.iurac.recruit.security;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.util.ByteSource;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * 这段代码定义了一个名为MyByteSource的自定义类，该类实现了Shiro的ByteSource接口并实现了序列化。
 * ByteSource是Shiro用于处理字节数据的接口，而MyByteSource提供了多种方式来创建ByteSource对象，并包含了一些与字节数据相关的方法。
 * */
public class MyByteSource implements ByteSource, Serializable {
    private  byte[] bytes; //字节数组，存储实际的字节数据
    private String cachedHex; //缓存的十六进制和Base64编码的字符串，以避免多次计算
    private String cachedBase64;

// 有多个构造方法，允许从不同类型的数据（如字节数组、字符数组、字符串、ByteSource、文件和输入流）创建MyByteSource对象。
    public MyByteSource(){}
    public MyByteSource(byte[] bytes) {this.bytes = bytes;}
    public MyByteSource(char[] chars) {this.bytes = CodecSupport.toBytes(chars);}
    public MyByteSource(String string) {this.bytes = CodecSupport.toBytes(string);}
    public MyByteSource(ByteSource source) {this.bytes = source.getBytes();}

    public MyByteSource(File file) {
        this.bytes = (new BytesHelper()).getBytes(file);
    }

    public MyByteSource(InputStream stream) {
        this.bytes = (new BytesHelper()).getBytes(stream);
    }

    //静态方法，用于检查传入的对象是否是兼容的数据类型（如字节数组、字符数组、字符串、ByteSource、文件和输入流）。
    public static boolean isCompatible(Object o) {
        return o instanceof byte[] || o instanceof char[] || o instanceof String || o instanceof ByteSource || o instanceof File || o instanceof InputStream;
    }

    public byte[] getBytes() {return this.bytes;}

    public boolean isEmpty() {return this.bytes == null || this.bytes.length == 0;}

    //将字节数组转换为十六进制和Base64编码的字符串。为了提高性能，结果被缓存，如果已经计算过，则直接返回缓存的结果。
    public String toHex() {
        if (this.cachedHex == null) {
            this.cachedHex = Hex.encodeToString(this.getBytes());
        }
        return this.cachedHex;
    }

    public String toBase64() {
        if (this.cachedBase64 == null) {
            this.cachedBase64 = Base64.encodeToString(this.getBytes());
        }
        return this.cachedBase64;
    }

    public String toString() {return this.toBase64();}

    public int hashCode() {
        return this.bytes != null && this.bytes.length != 0 ? Arrays.hashCode(this.bytes) : 0;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ByteSource) {
            ByteSource bs = (ByteSource)o;
            return Arrays.equals(this.getBytes(), bs.getBytes());
        } else {
            return false;
        }
    }

    //私有静态内部类，继承自CodecSupport，用于处理从文件和输入流获取字节数据的逻辑。
    private static final class BytesHelper extends CodecSupport {
        private BytesHelper() {
        }

        public byte[] getBytes(File file) {
            return this.toBytes(file);
        }

        public byte[] getBytes(InputStream stream) {
            return this.toBytes(stream);
        }
    }
}
