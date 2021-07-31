package IndexManager;
import java.util.Vector;
public class MyBPlusTree<K extends Comparable<? super K>, V> {
    /*ʵ�־�̬�Ļ���*/
    static abstract   class BaseNode<K extends Comparable<? super K>, V>{
        protected IntermediateNode<K,V> Father;//���ڵ�һ�����м�ڵ�
        protected Integer Order;//��¼����
        protected Integer Count;//��¼��ǰ�ڵ�ʹ���˼�����
        protected K[] Keys;//�����ֵlist�����ǻ�û�г�ʼ��


        public BaseNode(Integer _order) {
            this.Count=0;
            this.Order=_order;
            this.Father=null;
            this.Keys= (K[]) new Comparable[_order];
        }

        /*�����������Ҫ�ķ���*/
        //TODO
        public abstract V find(K key);

        //TODO
        public abstract Vector<V> findBigger(K key);

        //TODO
        public abstract LeafNode<K,V> getFirstLeafNode();

        //TODO
        public abstract BaseNode<K,V> insert(K key,V value);

        //TODO
        public abstract BaseNode<K,V> delete(K key);

        //TODO
        public abstract void update(K key,V value);

        //TODO
        public  boolean isRoot(){return this.Father==null;}

        //TODO
        public abstract void preOrderPrint();

        //TODO
        public abstract void checkStructure();

        //TODO
        public abstract K getLeast();


        protected int findIndex(K key){
            /*
             *@NOTE  :find the index of key use binary search,key:primary key such as ID,value is the address of specific tuple;
             *@param :key
             *@return:index in the key
             */
            //TODO
            /*Case1 nothing in the node*/
            if(this.Count==0)return 0;
            /*Case2 this.Count>0*/

            int index=0;
            int left=0;
            int right=this.Count;
            for(;left<right;){
                index=(left+right)/2;
                int result=key.compareTo(Keys[index]);
                if(result==0) break;
                else if(result>0) left=index+1;
                else if(result<0) right=index;
            }
            /*make sure keys[index]>=*/
            if (key.compareTo(this.Keys[index]) <= 0) {
                return index;
            }
            return ++index;
        }


        protected void updateDeletedKeys(K k1/*old*/,K k2/*new*/){
            /*
             *@NOTE  :as name of function
             *@param :
             *@return:
             */
            for(BaseNode<K,V> Node = this.Father/*��¼���ڵ�*/;Node!=null;Node=Node.Father/*���Ÿ��ڵ�Ѱ��*/){
                int position=Node.findIndex(k1);//�ҵ�ɾ����λ��
                if(Node.Keys[position].equals(k1)){
                    Node.Keys[position]=k2;//�Ѽ�ֵ����
                    return;
                }
            }
        }
    }
    private BaseNode<K,V> Root;
    public MyBPlusTree(Integer m) { //order: number of keys in one node
        this.Root = new LeafNode<>(m);
    }
    public Vector<V> find_eq(K key) {
        /*
         *@NOTE  :find the value that key == key
         *@param :key
         *@return:vector
         */
        //TODO
        Vector<V> res = new Vector<>();
        try {
            res.add(this.Root.find(key));
        } catch (IllegalArgumentException e) {
            //TODO
        }
        return res;
    }
    public Vector<V> find_neq(K key) {
        /*
         *@NOTE  :find address whose key ��= key
         *@param :key
         *@return:vector of address
         */
        //TODO
        Vector<V> addResult = new Vector<>();
        if (this.Root != null) {
            for(LeafNode<K, V> leaf = this.Root.getFirstLeafNode();leaf != null/*����Ϊ��*/;leaf = leaf.Pointer/*�ƶ�����һ���ڵ�*/){
                if (leaf.Keys[leaf.Count - 1].compareTo(key) < 0 || leaf.Keys[0].compareTo(key) > 0) {
                    /*��������ļ�ֵ���ڷ�Χ�ڣ�ȫ�����*/
                    for (int i = 0; i < leaf.Count; i++) {
                        addResult.add(leaf.Values[i]);
                    }
                }
                else {
                    int index = leaf.findIndex(key);//�����ҵ�����
                    if (leaf.Keys[index].equals(key)) {//��������ļ�ֵ����
                        //�������жϵķ�ʽ�������ÿһ��Ԫ�鶼�жϵĻ����˷�ʱ��
                        for (int i = 0; i < index; i++) {
                            addResult.add(leaf.Values[i]);
                        }
                        for (int i = index + 1; i < leaf.Count; i++) {
                            addResult.add(leaf.Values[i]);
                        }
                    } else {//�����ļ�ֵ������
                        for (int i = 0; i < leaf.Count; i++) {
                            addResult.add(leaf.Values[i]);
                        }
                    }
                }
            }
        }
        return addResult;
    }
    public Vector<V> find_leq(K key) {
        /*
         *@NOTE  :<=
         *@param :
         *@return:
         */
        //TODO
        Vector<V> res = this.find_less(key);
        try {
            V value = this.find_eq(key).get(0);
            res.add(value);
        } catch (IllegalArgumentException e) {
            //do nothing
        }
        return res;
    }
    public Vector<V> find_less(K key) {
        /*
         *@NOTE  :find keys smaller than key
         *@param :the threshold key
         *@return:vector of address
         */
        //TODO
        Vector<V> addResult = new Vector<>();
        if (this.Root != null) {
            for(LeafNode<K, V> leaf = this.Root.getFirstLeafNode();leaf != null/*����Ϊ��*/;leaf = leaf.Pointer/*�ƶ�����һ���ڵ�*/){
                if (leaf.Keys[leaf.Count - 1].compareTo(key) < 0 ){
                    /*��������ļ�ֵ���ڷ�Χ�ڣ�ȫ�����*/
                    for (int i = 0; i < leaf.Count; i++) {
                        addResult.add(leaf.Values[i]);
                    }
                }
                else {
                    int index = leaf.findIndex(key);//�����ҵ�����
                    //�������жϵķ�ʽ�������ÿһ��Ԫ�鶼�жϵĻ����˷�ʱ��
                    for (int i = 0; i < index; i++) {
                        addResult.add(leaf.Values[i]);
                    }
                    break;
                }

            }
        }
        return addResult;
    }
    public Vector<V> find_geq(K key) {
        /*
         *@NOTE  :>=
         *@param :
         *@return:
         */
        //TODO
        Vector<V> res = new Vector<>();
        try {
            V value = this.find_eq(key).get(0);
            res.add(value);
        } catch (IllegalArgumentException e) {
            //do nothing
        }
        res.addAll(this.find_greater(key));
        return res;
    }
    public Vector<V> find_greater(K key) {
        /*
         *@NOTE  :find that satisfy >
         *@param :
         *@return:
         */
        //TODO
        return this.Root.findBigger(key);
    }
    public void insert(K key, V value) throws IllegalArgumentException {
        /*
         *@NOTE  :insert map(primary:address)
         *@param :map(primary:address)
         *@return:none
         */
        //TODO
        BaseNode<K, V> result = Root.insert(key, value);
        if (result != null) {
            this.Root = result;
        }
    }
    public void delete(K key) throws IllegalArgumentException {
        /*
         *@NOTE  :delete map(primary:address)
         *@param :
         *@return:
         */
        //TODO
        BaseNode<K, V> ret = Root.delete(key);
        if (ret != null) {
            this.Root = ret;
        }
    }
    public void update(K key, V value) throws IllegalArgumentException {
        /*
         *@NOTE  :update map(primary:address)
         *@param :
         *@return:
         */
        //TODO
        this.Root.update(key, value);
    }
    public void print() {
        /*
         *@NOTE  :pre-order traversal
         *@param :
         *@return:
         */
        //TODO
        this.Root.preOrderPrint();
    }
    public void checkStructure() throws RuntimeException { //used for debug: to check the legality of the structure of the B+ tree
        this.Root.checkStructure();
    }
    /*ʵ���м�ڵ�*/
    static class IntermediateNode <K extends Comparable<? super K>, V> extends BaseNode<K, V>{

        protected BaseNode<K,V>[] Sons;

        public  IntermediateNode(Integer Order){
            super(Order);
            this.Sons=new BaseNode[Order+1];
        }

        @Override
        public V find(K key){
            /*
             *@NOTE  :use recursive to search the address value of key(primary)
             *@param :primary key
             *@return:address record in leaf node
             */
            //TODO
            int i=0;
            for(;/*�������н��*/i<this.Count&&/*��������Ҫ���Ҽ�ֵ*/key.compareTo(this.Keys[i])>=0;i++);
            if (this.Count == i/*���ָ�����ұ�*/ && /*������ұߵĺ��Ӳ�����*/this.Sons[this.Count] == null)
                return null;
            return /*�������Һ��ӽڵ�*/Sons[i].find(key);
        }

        @Override
        public Vector<V> findBigger(K key){
            /*
             *@NOTE  :use recursive to search the address value of key(primary)
             *@param :key
             *@return:vector address result
             */
            //TODO
            int i=0;
            for(;i<this.Count&&key.compareTo(this.Keys[i])>=0;i++);
            if (this.Count == i && this.Sons[this.Count] == null)
                return null;
            return Sons[i].findBigger(key);
        }
        @Override
        public LeafNode<K, V> getFirstLeafNode() {
            return this.Sons[0].getFirstLeafNode();
        }
        @Override
        public void update(K k,V v){
            /*
             *@NOTE  :update value,from left to right to recursive
             *@param :
             *@return:
             */
            //TODO
            int i=0;
            for(;i<this.Count&&k.compareTo(this.Keys[i])>=0;++i);
            this.Sons[i].update(k,v);
        }

        @Override
        public void preOrderPrint() throws RuntimeException {
            /*
             *@NOTE  :pre order traversal
             *@param :
             *@return:
             */
            //TODO
            for (int i = 0; i < this.Count; i++) {
                System.out.println(this.Keys[i] + " ");
            }
            System.out.println("\n");
            for (int i = 0; i <= this.Count; i++) {
                this.Sons[i].preOrderPrint();
                if (this.Sons[i].Father != this) {
                    throw new RuntimeException();
                }
            }
        }

        @Override
        public void checkStructure() throws RuntimeException{
            /*
             *@NOTE  :to check whether the internal node is valid
             *@param :
             *@return:
             */
            //TODO
            this.Sons[0].checkStructure();//i=1

            for(int i=1;i<=this.Count;i++){
                if(this.Keys[i-1]!=this.Sons[i].getLeast())
                    throw new RuntimeException();
                this.Sons[i].checkStructure();

            }
        }

        @Override
        public K getLeast() {
            return this.Sons[0].getLeast();
        }

        private IntermediateNode<K, V> insertSplit(K newKey, BaseNode<K, V> newChild, int index) { //insert the new child with the new key into the node at the specific index and split the node into 2
            /*
             *@NOTE  :for the internal node,if have to split
             *@param :the new key the
             *@return:the new node created
             */
            //TODO
            IntermediateNode<K, V> newNode = new IntermediateNode<>(this.Order);
            int pos;
            if (index < this.Order / 2) {
                pos = this.Order / 2;
                this.Keys[pos - 1] = null;
                for (int i = pos; i < this.Order; i++) {
                    newNode.Keys[i - this.Order / 2] = this.Keys[i];
                    this.Sons[i].Father = newNode;
                    newNode.Sons[i - this.Order / 2] = this.Sons[i];
                    this.Keys[i] = null;
                    this.Sons[i] = null;
                }
                this.Sons[this.Order].Father = newNode;
                newNode.Sons[this.Order - this.Order / 2] = this.Sons[this.Order];
                this.Sons[this.Order] = null;
                this.Count = this.Order / 2 - 1;
                newNode.Count = this.Order - this.Order / 2;
                this.insertIntoArray( newKey,newChild, index);
            } else if (index == this.Order / 2) {
                pos = this.Order / 2; //the new key inserted at the middle, thus split at the middle
                for (int i = pos; i < this.Order; i++) {
                    newNode.Keys[i - this.Order / 2] = this.Keys[i];
                    this.Sons[i + 1].Father = newNode;
                    newNode.Sons[i - this.Order / 2 + 1] = this.Sons[i + 1];
                    this.Keys[i] = null;
                    this.Sons[i + 1] = null;
                }
                newChild.Father = newNode;
                newNode.Sons[0] = newChild;
                this.Count = this.Order / 2;
                newNode.Count = this.Order - this.Order / 2;
            } else {
                pos = this.Order / 2 + 1; //the new key inserted after the middle, thus split at middle + 1
                this.Keys[pos - 1] = null;
                for (int i = pos; i < this.Order; i++) {
                    newNode.Keys[i - pos] = this.Keys[i];
                    this.Sons[i].Father = newNode;
                    newNode.Sons[i - pos] = this.Sons[i];
                    this.Keys[i] = null;
                    this.Sons[i] = null;
                }
                this.Sons[this.Order].Father = newNode;
                newNode.Sons[this.Order - pos] = this.Sons[this.Order];
                this.Sons[this.Order] = null;
                this.Count = this.Order / 2; //update the count
                newNode.Count = this.Order - pos; //update the count
                newNode.insertIntoArray( newKey,newChild, index - pos);
            }
            return newNode;
        }

        @Override
        public BaseNode<K,V> insert(K k,V v) {
            /*
             *@NOTE  :from left to right to recursive
             *@param :
             *@return:
             */
            //TODO
            int i=0;
            for(;i<this.Count&&k.compareTo(this.Keys[i])>=0;i++);
            return this.Sons[i].insert(k,v);
        }

        @Override
        public BaseNode<K, V> delete(K k) throws IllegalArgumentException {
            /*
             *@NOTE  :from left to right recursive to do
             *@param :
             *@return:
             */
            //TODO
            int i=0;
            for(;i<this.Count&&k.compareTo(this.Keys[i])>=0;++i);
            return this.Sons[i].delete(k);
        }

        public BaseNode<K,V> insertNode(BaseNode<K,V> node,K k){
            /*
             *@NOTE  :insert a internal or leaf node to this node
             *@param :
             *@return:
             */
            //TODO
            int position = findIndex(k);/*�Ҵ�Ӧ�ò����λ��*/
            if(this.Count<this.Order){
                this.insertIntoArray(k,node,position);
                return null;
            }
            K oldKey;
            if (position < this.Order / 2) {
                oldKey = this.Keys[this.Order / 2 - 1];
            } else if (position == this.Order / 2) {
                oldKey = k;
            } else {
                oldKey = this.Keys[this.Order / 2];
            }
            IntermediateNode<K, V> newNode = this.insertSplit(k, node, position);
            if (this.Father == null) {
                IntermediateNode<K, V> newParent = new IntermediateNode<>(this.Order);
                newParent.Keys[0] = oldKey;
                newParent.Count = 1;
                this.Father = newParent;
                newParent.Sons[0] = this;
                newNode.Father = newParent;
                newParent.Sons[1] = newNode;
                return newParent;
            }
            return this.Father.insertNode(newNode, oldKey);

        }

        private void insertIntoArray(K k, BaseNode<K,V> node,int position){
            /*
             *@NOTE  :put the map(primary key:node) in right place
             *@param :
             *@return:
             */
            //TODO
            for(int i=this.Count;i>position;i--){
                this.Keys[i]=this.Keys[i-1];
                this.Sons[i+1]=this.Sons[i];
            }
            this.Keys[position]=k;
            this.Sons[position+1]=node;
            node.Father=this;
            this.Count++;
        }

        private void deleteFromArray(int index) { //delete the key at index and the Sons at index + 1
            for (int i = index + 1; i < this.Count; i++) {
                this.Keys[i - 1] = this.Keys[i];
                this.Sons[i] = this.Sons[i + 1];
            }
            this.Keys[this.Count - 1] = null;
            this.Sons[this.Count] = null;
            this.Count--;
        }

        private int findNodeIndex(BaseNode<K, V> node) {
            for (int i = 0; i <= this.Count; i++) {
                if (this.Sons[i] == node) {
                    return i;
                }
            }
            return -1;
        }

        protected void updateDeletedKeys(K oldKey, K newKey) {
            BaseNode<K, V> node = this.Father;
            while (node != null) {
                int index = node.findIndex(oldKey);
                if (node.Keys[index].equals(oldKey)) {
                    node.Keys[index] = newKey; //update old key to new key
                    return;
                }
                node = node.Father;
            }
        }


        private IntermediateNode<K, V> getLeftBro() {
            int index = this.Father.findNodeIndex(this);
            return index > 0 ? (IntermediateNode<K, V>) this.Father.Sons[index - 1] : null;
        }

        private IntermediateNode<K, V> getRightBro() {
            int index = this.Father.findNodeIndex(this);
            return index < this.Father.Count ? (IntermediateNode<K, V>) this.Father.Sons[index + 1] : null;
        }

        private BaseNode<K, V> deleteInternalNode(IntermediateNode<K, V> node) throws IllegalArgumentException {
            /*
             *@NOTE  :ɾ���м��Ҷ�ڵ�
             *@param :
             *@return:
             */
            //TODO
            int index = this.findNodeIndex(node);
            if (this.Sons[index] != node) {
                throw new IllegalArgumentException();
            }
            this.deleteFromArray(index - 1);
            /*���ȷ���Ϸ���ֱ��ɾ��*/
            if (this.Count >= this.Order / 2) {
                return null;
            }
            if (this.isRoot()) {
                if (this.Count > 0) {
                    return null;
                } else {
                    this.Sons[0].Father = null;
                    return this.Sons[0];
                }
            }

            /*ɾ��֮�󲻺Ϸ���������Ҫ���ֵܽڵ���ֲ����*/
            IntermediateNode<K, V> left = this.getLeftBro();
            IntermediateNode<K, V> right = this.getRightBro();
            if (left != null && left.Count > this.Order / 2) { //move a Sons from left sibling to this
                for (int i = this.Count; i > 0; i--) {
                    this.Keys[i] = this.Keys[i - 1];
                    this.Sons[i + 1] = this.Sons[i];
                }
                this.Keys[0] = this.Father.Keys[this.Father.findNodeIndex(this) - 1];
                this.Father.Keys[this.Father.findNodeIndex(this) - 1] = left.Keys[left.Count - 1]; //update the keys of this's Father
                this.Sons[1] = this.Sons[0];
                this.Sons[0] = left.Sons[left.Count]; //change the last Sons of left sibling to the first Sons of this
                this.Sons[0].Father = this;
                this.Count++;
                left.Keys[left.Count - 1] = null;
                left.Sons[left.Count] = null;
                left.Count--;
                return null;
            }
            if (right != null && right.Count > this.Order / 2) { //move a Sons from right sibling to this
                this.Keys[this.Count] = right.Father.Keys[right.Father.findNodeIndex(right) - 1];
                right.Father.Keys[right.Father.findNodeIndex(right) - 1] = right.Keys[0]; //update the keys of right's Father
                this.Sons[this.Count + 1] = right.Sons[0]; //change the first Sons of left sibling to the last Sons of this
                this.Sons[this.Count + 1].Father = this;
                this.Count++;
                for (int i = 0; i < right.Count - 1; i++) {
                    right.Keys[i] = right.Keys[i + 1];
                    right.Sons[i] = right.Sons[i + 1];
                }
                right.Sons[right.Count - 1] = right.Sons[right.Count];
                right.Sons[right.Count] = null;
                right.Keys[right.Count - 1] = null;
                right.Count--;
                return null;
            }
            //Case 5: need to merge
            if (right != null) {
                left = this;
            } else if (left != null) {
                right = this;
            } else {
                throw new IllegalArgumentException();
            }
            left.merge(right);
            return this.Father.deleteInternalNode(right); //after merging right, right should be deleted from its parent
        }

        public BaseNode<K,V> deleteLeafNode(LeafNode<K, V> node, K key) throws IllegalArgumentException {
            /*
             *@NOTE  :delete the leaf node and these cases are vary complicated
             *@param :the leaf node the k
             *@return:the node
             */
            //TODO
            int index = this.findIndex(key);
            if (this.Sons[index + 1] != node) {
                throw new IllegalArgumentException();
            }
            this.deleteFromArray(index);

            //Case 3: delete directly
            if (this.Count >= this.Order / 2) {
                return null;
            }
            if (this.isRoot()) {
                if (this.Count > 0) {
                    return null;
                } else {
                    this.Sons[0].Father = null;
                    return this.Sons[0];
                }
            }

            //Case 4: delete and move a Sons from sibling to this
            IntermediateNode<K, V> left = this.getLeftBro(),right = this.getRightBro();
            if (left != null && left.Count > this.Order / 2) { //move a Sons from left sibling to this
                for (int i = this.Count; i > 0; i--) {
                    this.Keys[i] = this.Keys[i - 1];
                    this.Sons[i + 1] = this.Sons[i];
                }
                this.Keys[0] = this.Sons[0].Keys[0];
                this.Sons[1] = this.Sons[0];
                this.Sons[0] = left.Sons[left.Count]; //change the last Sons of left sibling to the first Sons of this
                this.Sons[0].Father = this;
                this.Count++;
                this.updateDeletedKeys(this.Keys[0], left.Keys[left.Count - 1]); //update the Keys of this's ancestors
                left.Keys[left.Count - 1] = null;
                left.Sons[left.Count] = null;
                left.Count--;
                return null;
            }
            if (right != null && right.Count > this.Order / 2) { //move a Sons from right sibling to this
                this.Keys[this.Count] = right.Sons[0].Keys[0];
                this.Sons[this.Count + 1] = right.Sons[0]; //change the first Sons of left sibling to the last Sons of this
                this.Sons[this.Count + 1].Father = this;
                this.Count++;
                for (int i = 0; i < right.Count - 1; i++) {
                    right.Keys[i] = right.Keys[i + 1];
                    right.Sons[i] = right.Sons[i + 1];
                }
                right.Sons[right.Count - 1] = right.Sons[right.Count];
                right.Sons[right.Count] = null;
                right.Keys[right.Count - 1] = null;
                right.Count--;
                right.updateDeletedKeys(this.Keys[this.Count - 1], right.Sons[0].Keys[0]); //update the keys of right's ancestors
                return null;
            }

            //Case 5: need to merge
            if (right != null) {
                left = this;
            } else if (left != null) {
                right = this;
            } else {
                throw new IllegalArgumentException();
            }
            left.merge(right);
            return this.Father.deleteInternalNode(right); //after merging right, right should be deleted from its Father
        }

        private void merge(IntermediateNode<K, V> right) {
            /*
             *@NOTE  :merge two internal node
             *@param :the right node
             *@return:nothing
             */
            //TODO
            for (int i = 0; i < right.Count; i++) {
                this.Keys[this.Count + i + 1] = right.Keys[i];
                this.Sons[this.Count + i + 1] = right.Sons[i];
                right.Sons[i].Father = this;
            }
            this.Keys[this.Count] = right.Father.Keys[right.Father.findNodeIndex(right) - 1];
            this.Sons[this.Count + right.Count + 1] = right.Sons[right.Count];
            right.Sons[right.Count].Father = this;
            this.Count += right.Count + 1;
        }

    }
    /*ʵ��Ҷ�ӽڵ�*/
    static class LeafNode<K extends Comparable<? super K>, V> extends BaseNode<K, V>{
        private V[] Values;
        private LeafNode<K,V> Pointer;
        public LeafNode(Integer Order){
            super(Order);
            this.Values=(V[])new Comparable[Order];
            this.Pointer=null;
        }
        @Override
        public V find(K key) {
            /*
             *@NOTE  :find the key:address value
             *@param :
             *@return:
             */
            int index;
            int left=0;
            int right=this.Count;
            for(;left<right;){
                index=(left+right)/2;
                int result=key.compareTo(this.Keys[index]);
                if(result==0){
                    return this.Values[index];
                }
                else if(result>0){
                    left=index+1;
                }
                else if(result<0)
                {
                    right=index;
                }
            }
            throw new IllegalArgumentException();
        }

        @Override
        public Vector<V> findBigger(K k) {
            /*
             *@NOTE  :find bigger in one node
             *@param :key
             *@return:vector
             */
            //TODO
            /*this node*/
            Vector <V> result = new Vector<>();
            int index=this.findIndex(k);
            if(index<this.Count){
                if(k.equals(this.Keys[index]))index++;
                for(int i=index;i<this.Count;i++)
                    result.add(this.Values[i]);
            }
            /*next node*/
            for(LeafNode<K,V> Node=this.Pointer;Node!=null;Node=Node.Pointer)
                for(int i=0;i<this.Count;i++)
                    result.add(this.Values[i]);
            return result;
        }

        @Override
        public LeafNode<K, V> getFirstLeafNode() {
            /*
             *@NOTE  :���ص�һ��Ҷ�ڵ�
             *@param :
             *@return:
             */
            //TODO
            return this;
        }


        //        @Override
        public BaseNode<K, V> insertForTest(K key, V value) throws IllegalArgumentException {
            //Find the index of the key (binary search)
            int index = findIndex(key);

            if (index < this.Count && this.Keys[index].equals(key)) {
                throw new IllegalArgumentException("Key " + key + " already exists"); //already exists
            }
            if (this.Count < this.Order) { //needn't split
                this.insertIntoArray(key, value, index);
                return null;
            }

            LeafNode<K, V> newSibling = this.insertSplit(key, value, index); //split
            if (this.Father == null) { //this is root
                IntermediateNode<K, V> newParent = new IntermediateNode(this.Order);
                newParent.Keys[0] = newSibling.Keys[0];
                newParent.Count = 1;
                this.Father = newParent;
                newParent.Sons[0] = this;
                newSibling.Father = newParent;
                newParent.Sons[1] = newSibling;
                return newParent; //new parent is the new root
            }
            return this.Father.insertNode(newSibling, newSibling.Keys[0]); //insert the new node to this's parent
        }

        @Override
        public BaseNode<K, V> insert(K k, V v) {
            /*
             *@NOTE  :insert into leaf node
             *@param :
             *@return:
             */
            //TODO
            int index = findIndex(k);
            if(index<this.Count&&k.equals(this.Keys[index]))
                throw new IllegalArgumentException("Key :" + k + " is already exists!");
            /*the node has enough place to insert*/
            if(this.Count<this.Order){
                this.insertIntoArray(k,v,index);
                return null;
            }
            /*Count>=Order in leaf node , which means have to split*/
            LeafNode<K,V> newBro=insertSplit(k,v,index);
            if(this.Father==null/*if root is this leaf root*/){
                IntermediateNode<K,V> newFather = new IntermediateNode<>(this.Order);
                newFather.Keys[0]=newBro.Keys[0];/*���ڵ��һ�������µ��ֵܽڵ�ĵ�һ��*/
                newFather.Count=1;
                this.Father=newFather;
                newFather.Sons[0]=this;
                newBro.Father=newFather;
                newFather.Sons[1]=newBro;
                return newFather;
            }
            return this.Father.insertNode(newBro,newBro.Keys[0]);/*���µĽڵ���뵽���ڵ���*/
        }

        private LeafNode<K,V> insertSplit(K newk,V newv, int position){
            /*
             *@NOTE  :when current is full so insert node split
             *@param :
             *@return:new leaf node
             */
            //TODO
            LeafNode<K,V> newBro=new LeafNode<>(this.Order);
            int positionSplit;
            if(position<=this.Order/2){
                /*����µļ�ֵ�������м��֮ǰ���ʹ��м��ֿ�*/
                positionSplit=this.Order/2;
                for(int i = positionSplit;i<this.Order;++i){
                    /*Ϊ�µĽڵ㸳ֵ*/
                    newBro.Keys[i-this.Order/2]=this.Keys[i];
                    newBro.Values[i-this.Order/2]=this.Values[i];
                    /*��ǰ�ڵ�ɾ��*/
                    this.Values[i]=null;
                    this.Keys[i]=null;
                }
                this.Count=this.Order/2;//���µ�ǰ�ڵ�ĸ���
                newBro.Count=this.Order-this.Order/2;//�����½��ڵ�ĸ���
                this.insertIntoArray(newk,newv,position);//�����Ͳ��뵽���ڵ��ж������뵽�µĽڵ���
            }
            else{
                /*����²����λ�����м�λ��֮����ô�ʹ�middle+1���ֿ�*/
                positionSplit=this.Order/2+1;
                for(int i = positionSplit;i<position;++i){
                    /*Ϊ�µĽڵ㸳ֵ*/
                    newBro.Keys[i-(this.Order/2+1)]=this.Keys[i];
                    newBro.Values[i-(this.Order/2+1)]=this.Values[i];
                    /*��ǰ�ڵ�ɾ��*/
                    this.Values[i]=null;
                    this.Keys[i]=null;
                }
                /*���뵽�µĽڵ���*/
                newBro.Keys[position-(this.Order/2+1)]=newk;
                newBro.Values[position-(this.Order/2+1)]=newv;
                for(int i = position;i<this.Order;++i){
                    /*Ϊ�µĽڵ㸳ֵ*/
                    newBro.Keys[i-this.Order/2]=this.Keys[i];
                    newBro.Values[i-this.Order/2]=this.Values[i];
                    /*��ǰ�ڵ�ɾ��*/
                    this.Values[i]=null;
                    this.Keys[i]=null;
                }
                this.Count=this.Order/2+1;
                newBro.Count=this.Order-this.Order/2;
            }
            LeafNode<K, V> temp = this.Pointer;//�õ���ǰ�ڵ����һ���ڵ�
            this.Pointer=newBro;//��ǰ�ڵ�ָ���µĽڵ�
            newBro.Pointer=temp;//�´����Ľڵ�ָ��ԭ������һ���Ľڵ㣬ά������
            return newBro;
        }


        private void insertIntoArray(K k,V v,int index){
            /*
             *@NOTE  :insert into Node list but have to move the key:value pair backward
             *@param :
             *@return:
             */
            //TODO
            for(int i = this.Count;i>index;i--){
                this.Keys[i]=this.Keys[i-1];
                this.Values[i]=this.Values[i-1];
            }
            this.Keys[index]=k;
            this.Values[index]=v;
            this.Count++;
        }

        @Override
        public BaseNode<K, V> delete(K key) {
            /*
             *@NOTE  :delete the map(key:value)
             *@param :
             *@return:
             */
            //TODO
            int position= this.findIndex(key);
            if(key.compareTo(this.Keys[position])!=0){/*���뱣֤Ҫɾ�����Ǵ��ڵ�*/
                throw new IllegalArgumentException("Key :" + key + " not found");
            }
            this.deleteFromArray(position);//ɾ������

            /*1.ֱ��ɾ��*/
            if(this.isRoot())return null;
            if(this.Count>=(this.Order+1)/2){/*����ɾ��֮����Ȼ�Ϸ�*/
                if(position==0/*������0λ��*/)this.updateDeletedKeys(key,this.Keys[0]);/*ɾ��֮����Ҫ���õݹ�������е��м�ڵ�*/
                return null;
            }


            /*2.ɾ��֮����ֵܽڵ��Ų����*/
            K oldk;
            if(position!=0)
                oldk=this.Keys[0];/*˵�������ұ�*/
            else
                oldk=key;/*������Ѿ�������*/

            LeafNode<K,V> left=this.getLeftBro(oldk);
            LeafNode<K,V> right=this.getRightBro();
            /*���Դ�����ֵܽڵ���һ��*/
            //TODO
            if(left!=null&&left.Count>(this.Order+1)/2){
                K borrowKey=left.Keys[left.Count-1];
                V borrowValue=left.Values[left.Count-1];
                this.insertIntoArray(borrowKey,borrowValue,0);
                /*��ڵ����Ҳ�ɾ��*/
                left.Keys[left.Count-1]=null;
                left.Values[left.Count-1]=null;
                left.Count--;
                /*��������*/
                this.updateDeletedKeys(oldk,this.Keys[0]);
                return null;
            }
            /*��߽費�ˣ����ұ߿�ʼ��*/
            if(right!=null&&right.Count>(this.Order+1)/2){
                this.Keys[this.Count]=right.Keys[0];
                this.Values[this.Count]=right.Values[0];
                this.Count++;
                /*�ҽڵ������ɾ��*/
                right.deleteFromArray(0);
                /*��������*/
                right.updateDeletedKeys(this.Keys[this.Count-1],right.Keys[0]);
                if(position==0)this.updateDeletedKeys(oldk,this.Keys[0]);
                return null;
            }

            /*3.ɾ��֮��ϲ�*/
            if (right != null) {
                left = this;
                if (position == 0) {
                    //update the keys of this's ancestors
                    if (this.Count > 0) {
                        this.updateDeletedKeys(oldk, this.Keys[0]);
                    } else {
                        this.updateDeletedKeys(oldk, right.Keys[0]);
                    }
                }
                oldk = right.Keys[0];
            } else if (left != null) {
                right = this;
            } else {
                throw new IllegalArgumentException();
            }
            left.merge(right);
            return this.Father.deleteLeafNode(right, oldk);
        }

        private void merge(LeafNode<K, V> right) throws IllegalArgumentException {
            if (this.Pointer != right/*����ָ���ҽڵ�*/) {
                throw new IllegalArgumentException();
            }
            /*���ұ߽ڵ�����ݿ�������*/
            int i=0;
            while(i < right.Count){
                this.Keys  [this.Count + i] = right.Keys  [i];
                this.Values[this.Count + i] = right.Values[i];
                ++i;
            }
            this.Count += right.Count;
            this.Pointer = right.Pointer;
        }

        private LeafNode<K,V> getLeftBro(K k){
            /*
             *@NOTE  :get left sibling
             *@param :
             *@return:
             */
            //TODO
            if(this.isRoot())return null;
            int postion = this.Father.findIndex(k);
            LeafNode<K, V> left = (LeafNode<K, V>)this.Father.Sons[postion];
            if(left.Pointer!=this)return null;
            return left;
        }
        private LeafNode<K,V> getRightBro(){
            /*
             *@NOTE  :return right sibling
             *@param :
             *@return:
             */
            //TODO
            if(this.isRoot())return null;
            LeafNode<K, V> right = this.Pointer;
            if(right==null||right.Father!=this.Father)return null;
            return right;
        }


        private void deleteFromArray(int index) {
            for (int i = index + 1; i < this.Count; i++) {
                this.Keys[i - 1] = this.Keys[i];
                this.Values[i - 1] = this.Values[i];
            }
            this.Keys[this.Count - 1] = null;
            this.Values[this.Count - 1] = null;
            this.Count--;
        }

        @Override
        public void update(K key, V value) throws IllegalArgumentException {
            /*
             *@NOTE  :Update the value
             *@param :
             *@return:
             */
            //TODO
            int position = findIndex(key);
            if(position>this.Count||key.compareTo(this.Keys[position])!=0)
                throw new IllegalArgumentException("Key " + key + " not found"); //not found
            this.Values[position]=value;
        }

        @Override
        public void preOrderPrint() {
            /*
             *@NOTE  :preorder traversal
             *@param :
             *@return:
             */
            //TODO
            int i =0;
            while(i<this.Count){
                System.out.println(this.Keys[i] + ":" + this.Values[i] + " ");
                ++i;
            }
            System.out.println("\n");
        }

        @Override
        public void checkStructure() throws RuntimeException {
            return;
        }

        @Override
        public K getLeast() {
            return this.Keys[0];
        }
    }
    public static void main(String[] args){

        MyBPlusTree<Integer, String>testtree =new MyBPlusTree<Integer, String>(6);
        for(int i=0;i<5000000;++i)
        {
            testtree.insert(i,String.valueOf(i));
        }
        for(int i=0;i<4999990;++i)
        {
            testtree.delete(i);
        }

//        testtree.print();
//        testtree.checkStructure();

        Vector<String> result = testtree.find_greater(123456);
        for(String i:result){
            System.out.println(i);
        }
        System.out.println("Complete!��");
    }
}
