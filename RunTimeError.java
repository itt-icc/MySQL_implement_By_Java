public class RunTimeError extends Exception{
    public final static String type="Runtime error"; //exception type->Runtime error
    public String msg; //exception message

    RunTimeError(String m) {
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
