package BufferManager;

public class Block {
    public static final int BlockSize=4096;//
    private int LRU=0;//time being used(LRU-algorithm)
    private int BlockOffset = 0; //the block offset in the file
    private boolean isChanged = false; //whether being changed in the data
    private boolean isValid = false; //true if there is related file in disk
    private boolean isLocked = false; //true is this block's data is being used
    private String FileName=""; //corresponding file name of this block
    private byte[] Data=new byte[BlockSize];//1 Block has 4B data

    public Block(){}//initialization

    /**
     * Function: resetMode()
     * Description:Reset the mode of this Block
     * @param:void
     * @return:void
     */
    public void resetMode(){
        isChanged=isValid=isLocked=false;//for initialization or reset
        //Arrays.fill(Data,(byte)0); initialize data with 0
        LRU = 0;
    }

    /**
     * Function: dataWrite()
     * Description: Write data into the block (for char 1 char=1 byte)
     * @param offset: the offset in this block(0-4096)
     * @param data: the data want to be stored
     * @return boolean: true if data write successful
     */
    public boolean dataWrite(int offset, byte[] data){
        if (offset+data.length>BlockSize)
            return false;
        else
            for(int i =0; i<data.length;i++)
            {
                Data[offset+i]=data[i];
            }
            isChanged=true;
            LRU=LRU+1;
            return true; //write data successfully

    }

    /**
     * Function: intRead()
     * Description: read integer from block in certain position
     * @param offset: the offset in this block(0-4096)
     * @return int: the integer stored
     */
    public int intRead(int offset){
        if (offset+4>BlockSize) return 0; //offset wrong, int not available TODO:exception
        //big-endian store-32bit TODO:64bit?
        int int0 = Data[offset] & 0xFF;
        int int1 = Data[offset + 1] & 0xFF;
        int int2 = Data[offset + 2] & 0xFF;
        int int3 = Data[offset + 3] & 0xFF;
        int result = (int0 << 24) | (int1 << 16) | (int2 << 8) | int3;
        return result; //write data successfully
    }

    /**
     * Function: intWrite()
     * Description: write integer into this block, this function just for convenience
     * @param offset: the offset in this block(0-4096)
     * @param data: the data wanted to be stored
     * @return boolean: true if successful
     */
    public boolean intWrite(int offset, int data){
        if (offset+4>BlockSize) return false; //offset wrong, int not available TODO:exception
        //big-endian store
        Data[offset] =(byte)(data>>24 & 0xFF);
        Data[offset+1] =(byte)(data>>16 & 0xFF);
        Data[offset+2] =(byte)(data>>8 & 0xFF);
        Data[offset+3] =(byte)(data & 0xFF);
        LRU=LRU+1;
        isChanged=true;
        return true;
    }

    /**
     * Function: floatRead()
     * Description: read float from block in certain position
     * @param offset: the offset in this block(0-4096)
     * @return float: the integer stored
     */
    public float floatRead(int offset){
        int data = intRead((offset));
        float result = Float.intBitsToFloat(data);
        return result;
    }

    /**
     * Function: floatWrite()
     * Description: write float into this block, this function just for convenience
     * @param offset: the offset in this block(0-4096)
     * @param data: the data wanted to be stored
     * @return boolean: true if successful
     */
    public boolean floatWrite(int offset, float data){
        int int_data =Float.floatToIntBits(data);
        return intWrite(offset,int_data);
    }

    /**
     * Function: stringRead()
     * Description: read string from block in certain position
     * @param offset: the offset in this block(0-4096)
     * @return string: the integer stored
     */
    public String stringRead(int offset, int length){
        byte[] result = new byte[length];// no more than the length of block
        for (int i = 0; i<length && i<BlockSize-offset; i++)
            result[i]=Data[offset+i];
        LRU = LRU+1;
        return new String(result);
    }

    /**
     * Function: stringWrite()
     * Description: write string into this block, this function just for convenience
     * @param offset: the offset in this block(0-4096)
     * @param str: the data wanted to be stored
     * @return boolean: true if successful
     */
    public boolean stringWrite(int offset, String str){
        byte[] data = str.getBytes();
        return dataWrite(offset,data);
    }

    /*
    following are condition check function
     */
    /**
     * Function: isChanged()
     * Description: true if this block has been changed--> for data from buffer2disk
     */
    public boolean isChang(){return this.isChanged;}

    /**
     * Function: isLocked()
     * Description: true if this block is being used
     */
    public boolean isLock(){return this.isLocked;}

    /**
     * Function: isValided()
     * Description: true if this block is legal
     */
    public boolean isValided(){return this.isValid;}

    /**
     * Function: getFileName()
     * Description: get which file this block belongs to
     */
    public String getFileName(){return this.FileName;}

    /**
     * Function: getBlock()
     * Description: get which part of file this block belongs to
     */
    public int getBlock(){return this.BlockOffset;}

    /**
     * Function: getLRU()
     * Description: how many times this block has been used-->implement LRU algorithm
     */
    public int getLRU(){return this.LRU;}

    /**
     * Function: getData()
     * Description: obtain the data this block stores
     */
    public byte[] getData(){return this.Data;}

    /**
     * Function: setData()
     * Description: set whole block data-->true if the size fits
     */
    public boolean setData(byte[] data){
        if (data.length==BlockSize)
        {
            this.Data=data;
            return true;
        }
        else return false;
    }

    /**
     * Function: setChange()
     * Description: set the status of isChanged
     */
    public void setChange(boolean flag){this.isChanged=flag;}

    /**
     * Function: setValid()
     * Description: set the status of isValid
     */
    public void setValid(boolean flag){this.isValid=flag;}

    /**
     * Function: setLock()
     * Description: set the status of isLocked
     */
    public void setLock(boolean flag){this.isLocked=flag;}

    /**
     * Function: setFileName()
     * Description: assign which file this block belongs to
     */
    public void setFileName(String name){this.FileName=name;}

    /**
     * Function: setBlockOffset()
     * Description: assign in which part of file this block belongs to
     */
    public void setBlockOffset(int offset){this.BlockOffset=offset;}
}
