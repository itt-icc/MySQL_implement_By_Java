package CatalogManager;

public class Address implements Comparable<Address>{
    private String filename;
    private int blockOffset;
    private int byteOffset;

    public Address(String filename,int blockOffset,int byteOffset){
        this.filename = filename;
        this.blockOffset = blockOffset;
        this.byteOffset = byteOffset;
    }

    @Override
    //bytediff-->blockdiff-->filediff
    public int compareTo(Address address){
        if (this.filename.compareTo(address.filename) == 0){
            if (this.blockOffset ==address.blockOffset) return this.byteOffset-address.byteOffset;
            else return this.blockOffset-address.blockOffset;
        }
        else return this.filename.compareTo(address.filename);
    }

    public String getFilename(){return this.filename;}
    public int getBlockOffset(){return this.blockOffset;}
    public int getByteOffset(){return this.byteOffset;}
}

