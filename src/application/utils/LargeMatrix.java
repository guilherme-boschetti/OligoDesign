package application.utils;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;
 
import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

// https://www.javacodegeeks.com/2012/01/using-memory-mapped-file-for-huge.html

public class LargeMatrix implements Closeable {
    private static final int MAPPING_SIZE = 1 << 30;
    private final RandomAccessFile raf;
    private final int width;
    private final int height;
    private final List<MappedByteBuffer> mappings = new ArrayList<>();
 
    public LargeMatrix(String filename, int width, int height) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
        try {
            this.width = width;
            this.height = height;
            long size = 8L * width * height;
            for (long offset = 0; offset < size; offset += MAPPING_SIZE) {
                long size2 = Math.min(size - offset, MAPPING_SIZE);
                mappings.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, offset, size2));
            }
        } catch (IOException e) {
            raf.close();
            throw e;
        }
    }
 
    protected long position(int x, int y) {
        return (long) y * width + x;
    }
 
    public int width() {
        return width;
    }
 
    public int height() {
        return height;
    }
 
    public int get(int x, int y) {
        assert x >= 0 && x < width;
        assert y >= 0 && y < height;
        long p = position(x, y) * 8;
        int mapN = (int) (p / MAPPING_SIZE);
        int offN = (int) (p % MAPPING_SIZE);
        return mappings.get(mapN).getInt(offN);
    }
 
    public void set(int x, int y, int d) {
        assert x >= 0 && x < width;
        assert y >= 0 && y < height;
        long p = position(x, y) * 8;
        int mapN = (int) (p / MAPPING_SIZE);
        int offN = (int) (p % MAPPING_SIZE);
        mappings.get(mapN).putInt(offN, d);
    }
 
    public void close() throws IOException {
        for (MappedByteBuffer mapping : mappings)
            clean(mapping);
        raf.close();
    }
 
    private void clean(MappedByteBuffer mapping) {
        if (mapping == null) 
        	return;
        Cleaner cleaner = ((DirectBuffer) mapping).cleaner();
        if (cleaner != null) 
        	cleaner.clean();
    }
}
