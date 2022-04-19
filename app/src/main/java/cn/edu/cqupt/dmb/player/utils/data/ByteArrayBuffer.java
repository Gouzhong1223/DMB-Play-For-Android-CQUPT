package cn.edu.cqupt.dmb.player.utils.data;

/**
 * @Author : Gouzhong
 * @Blog : www.gouzhong1223.com
 * @Description : An expandable byte buffer built on byte array.
 * @Date : create by QingSong in 2022-04-18 18:50
 * @Email : qingsong.qs@alibaba-inc.com
 * @Since : JDK 1.8
 * @PackageName : cn.edu.cqupt.dmb.player.utils
 * @ProjectName : DMB Player For Android
 * @Version : 1.0.4
 */
public final class ByteArrayBuffer {

    private byte[] buffer;
    private int len;

    public ByteArrayBuffer(int capacity) {
        super();
        if (capacity < 0) {
            throw new IllegalArgumentException("Buffer capacity may not be negative");
        }
        this.buffer = new byte[capacity];
    }

    private void expand(int newLen) {
        byte[] newbuffer = new byte[Math.max(this.buffer.length << 1, newLen)];
        System.arraycopy(this.buffer, 0, newbuffer, 0, this.len);
        this.buffer = newbuffer;
    }

    public void append(final byte[] b, int off, int len) {
        if (b == null) {
            return;
        }
        if ((off < 0)
                || (off > b.length)
                || (len < 0)
                || ((off + len) < 0)
                || ((off + len) > b.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        int newlen = this.len + len;
        if (newlen > this.buffer.length) {
            expand(newlen);
        }
        System.arraycopy(b, off, this.buffer, this.len, len);
        this.len = newlen;
    }

    public void append(int b) {
        int newlen = this.len + 1;
        if (newlen > this.buffer.length) {
            expand(newlen);
        }
        this.buffer[this.len] = (byte) b;
        this.len = newlen;
    }

    public void append(final char[] b, int off, int len) {
        if (b == null) {
            return;
        }
        if ((off < 0)
                || (off > b.length)
                || (len < 0)
                || ((off + len) < 0)
                || ((off + len) > b.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        int oldLen = this.len;
        int newLen = oldLen + len;
        if (newLen > this.buffer.length) {
            expand(newLen);
        }
        for (int i1 = off, i2 = oldLen; i2 < newLen; i1++, i2++) {
            this.buffer[i2] = (byte) b[i1];
        }
        this.len = newLen;
    }

    public void clear() {
        this.len = 0;
    }

    public byte[] toByteArray() {
        byte[] b = new byte[this.len];
        if (this.len > 0) {
            System.arraycopy(this.buffer, 0, b, 0, this.len);
        }
        return b;
    }

    public int byteAt(int i) {
        return this.buffer[i];
    }

    public int capacity() {
        return this.buffer.length;
    }

    public int length() {
        return this.len;
    }

    public byte[] buffer() {
        return this.buffer;
    }

    public void setLength(int len) {
        if (len < 0 || len > this.buffer.length) {
            throw new IndexOutOfBoundsException();
        }
        this.len = len;
    }

    public boolean isEmpty() {
        return this.len == 0;
    }

    public boolean isFull() {
        return this.len == this.buffer.length;
    }
}
