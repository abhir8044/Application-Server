package appserver.job.impl;

import appserver.job.Tool;

public class Fib implements Tool{

    static public Integer fib(Integer number){
        if(number == 0){
            return 0;
        }
        else if(number == 1){
            return 1;
        }
        return fib(number - 1) + fib(number - 2);
    }
    
    
    @Override
    public Object go(Object parameters) {
        return Fib.fib((Integer) parameters);
    }
}