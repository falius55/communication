package jp.gr.java_conf.falius.communication.swapper;

/**
 * 一度の送受信で通信を終える際に利用するSwapperです。
 *
 * クライアントに限り、swapメソッドのreceiveDataにはnullが入っていますので注意してください。
 *
 * @author "ymiyauchi"
 * @since 1.0
 *
 */
public abstract class OnceSwapper implements Swapper {

    /**
     * {@inheritDoc}
     * @since 1.0
     */
    @Override
    public final boolean doContinue() {
        return false;
    }

}
