public class SnytaxError extends Exception{
    public final static String type="Snytax error"; //exception type->Runtime error
    public String msg; //exception message

    SnytaxError(String m) {
        msg=m;
    }

    @Override
    public String getMessage() {
        return this.type+" : "+this.msg;
    }

    public void printMsg() {
        System.out.println(this.getMessage());
    }
}
