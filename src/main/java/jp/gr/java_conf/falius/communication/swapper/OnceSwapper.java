package jp.gr.java_conf.falius.communication.swapper;

/**
 * 一度の送受信で通信を終える際に利用するSwapperです。
 *
 * クライアントに限り、swapメソッドのreceiveDataにはnullが入っていますので注意してください。
 *
 * @author "ymiyauchi"
 *
 */
public abstract class OnceSwapper implements Swapper {

    @Override
    public final boolean doContinue() {
        return false;
    }

}
