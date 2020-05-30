package ai.preferred.cerebro.webservice;

/**
 * @author hpminh@apcs.vn
 */


/**
 * singleton class to hooks up resourses and essential object
 */
public class ControllerHook {

    private static ControllerHook instance = null;
    private UpdateController updateController;
    private RecomController recomController;
    private final Object dummyLock;

    private ControllerHook(){
        dummyLock = new Object();
    }

    synchronized static public ControllerHook getInstance(){

        if(instance == null){
            instance = new ControllerHook();
        }
        return instance;
    }

    public void putUpdateController(UpdateController updateController){
        this.updateController = updateController;
        synchronized (dummyLock){
            if(recomController != null){
                hook();
            }
        }
    }

    public void putRecomController(RecomController recomController){
        this.recomController = recomController;
        synchronized(dummyLock){
            if(updateController != null){
                hook();
            }
        }
    }

    private void hook(){
        updateController.setParams(recomController.ratingsRespository, recomController, recomController.cornacURL);
    }
}
