package BufferManager;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class BufferManager {
    private static final int MaxNum = 64;
    private static Block[] buffer = new Block[MaxNum];

    public BufferManager(){}

    /**
     * Function: initBuffer()
     * Description:Reset/Init buffer
     */
    public static void initBuffer()
    {
        for (int i=0; i<MaxNum; i++)
        {
            buffer[i] = new Block();
            buffer[i].resetMode();
        }
    }

    /**
     * Function: deleteBuffer()
     * Description: Destroy buffer
     */
    public static void deleteBuffer()
    {
        for (int i=0; i<MaxNum; i++)
        {
            if(buffer[i].isChang()) Block2Disk(i);
        }
    }

    /**
     * Function: Disk2Block()
     * Description:read a whole block from disk
     * @param filename: read which file
     * @param block_offset: from which certain part of the file
     * @param index: read to which block in the buffer
     * @return: boolean
     */
    public static boolean Disk2Block(String filename, int block_offset, int index)
    {
        boolean flag=false;
        byte[] data = new byte[Block.BlockSize];
        RandomAccessFile rfile = null;
        try{
            File file = new File(filename);
            rfile = new RandomAccessFile(file,"rw");

            int len= (int) rfile.length();
            if ((block_offset+1)*Block.BlockSize<=len)
            {
                rfile.seek(block_offset*Block.BlockSize); //point to the position of file we want to obtain
                rfile.read(data,0,Block.BlockSize);// read into data
            }
            else
            {
                Arrays.fill(data,(byte)0); //if overflow, provide empty block
                //put extra into another block
//                int size = (int)rfile.length()-block_offset*Block.BlockSize;
//                byte[] tmp = new byte[size];
//                rfile.read(tmp,0,size);
//                for (int i = 0 ;i<size;i++) data[i]=tmp[i];
            }
            flag = true;
        }catch(Exception e){
            System.out.println(e.getMessage());//no such file
        }finally{
            try{
                if (rfile != null) rfile.close();
            }catch (Exception e){
                System.out.println((e.getMessage()));
            }
        }
        if(flag){
            buffer[index].resetMode();
            buffer[index].setData(data);
            buffer[index].setFileName(filename);
            buffer[index].setBlockOffset(block_offset);
            buffer[index].setValid(true);
        }
        return true;
    }
    /**
     * Function: readBlock()
     * Description: provide the certain block in the buffer
     * @param filename: read which file
     * @param block_offset: from which certain part of the file
     * @return: the Block
     */
    public static Block readBlock(String filename, int block_offset)
    {
        int i=0;
        // if this block is in the buffer
        for (;i<MaxNum;i++)
        {
            if (buffer[i].isValided() && buffer[i].getFileName().equals((filename)) && buffer[i].getBlock()==block_offset)
                break;
        }
        if (i < MaxNum) {  //there exist a block
            return buffer[i];
        }
        else {
            //if this block is not in the buffer
            File file = new File(filename);
            int freeId = getFreeBlockId();
            if(freeId==-1||!file.exists())return null;
            if (!Disk2Block(filename, block_offset, freeId)) return null;
            return buffer[freeId];
        }
    }

    /**
     * Function: Block2Disk(int index)
     * Description:write a whole block from buffer to disk
     * @param index: write which block to the disk
     * @return: boolean
     */
    private static boolean Block2Disk(int index){
        boolean flag = false;
        if (!buffer[index].isChang()) return true; //no change no write
        if (buffer[index].isValided()) //if this buffer is legal, we need to write
        {
        RandomAccessFile rfile =null;
        try {
            String filename = buffer[index].getFileName();
            File file = new File(filename);
            if (!file.exists()) file.createNewFile();
            rfile = new RandomAccessFile(file, "rw");
            rfile.seek(buffer[index].getBlock()*Block.BlockSize);
            rfile.write(buffer[index].getData());// 补零的是否需要再进行操作？
            flag = true;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally {
            try{
                if(rfile!=null) rfile.close();
            }catch (Exception e){
                System.out.println(e.getMessage());
                flag = false;
            }
        }
        }
        return flag;
    }
    /**
     * Function: getFreeBlockId()
     * Description: which block in the buffer can be used
     * @return: int
     */
    private static int getFreeBlockId(){
        int index=-1;
        int minLRU = 0x7FFFFFFF;
        for (int i=0;i<MaxNum;i++){
            if(!buffer[i].isValided() && !buffer[i].isLock()) return i;//invalid buffer directly provided
            if(!buffer[i].isLock() && buffer[i].getLRU()<minLRU){
                index = i;
                minLRU = buffer[i].getLRU();
            }
        }
        if (index != -1 && buffer[index].isChang())
            Block2Disk(index);
        return index;
    }
    /**
     * Function: setInvalid()
     * Description: set this block in buffer invalid
     * @return: void
     */
    public static void setInvalid(String filename){
        for (int i =0; i <MaxNum;i++)
            if (buffer[i].getFileName() != null && buffer[i].getFileName().equals(filename))
                buffer[i].setValid(false);
    }
}
