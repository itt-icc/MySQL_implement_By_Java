package IndexManager;

public class Index {

    public String indexName;
    public String tableName;
    public String attributeName;
    public int rootNum;
    public int blockNum = 0;

    public Index(String _indexName, String tableName, String attributeName, int blockNum, int rootNum) {
        this.indexName = _indexName;
        this.tableName = tableName;
        this.attributeName = attributeName;
        this.blockNum = blockNum;
        this.rootNum = rootNum;
    }

    public Index(String indexName, String tableName, String attributeName) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.attributeName = attributeName;
    }

}