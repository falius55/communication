package jp.gr.java_conf.falius.communication.swapper;

import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;

/**
 * 固定回数分送受信を行うためのSwapper
 *
 * @author "ymiyauchi"
 * @since 1.4.0
 *
 */
public abstract class FixedRepeatSwapper extends RepeatSwapper {
    private int mCount = 0;
    private final int mLimit;

    /**
     *
     * @param limit 送受信の最大回数。送信と受信のセットで一回です。
     * @since 1.4.0
     */
    public FixedRepeatSwapper(int limit) {
        mLimit = limit;
    }

    /**
     * @since 1.4.0
     */
    @Override
    public final SendData swap(String remoteAddress, ReceiveData receiveData) throws Exception {
        SendData sendData = onSwap(remoteAddress, receiveData);
        mCount++;
        if (mCount == mLimit) {
            finish();
            // このデータを送信して、最後に受信して終わり
            // nullをここで返すと直後に切断されてしまい、最後の送受信が行われない。
        }
        return sendData;
    }

    /**
     * @param remoteAddress
     * @param receiveData
     * @return
     * @throws Exception
     * @since 1.4.0
     */
    public abstract SendData onSwap(String remoteAddress, ReceiveData receiveData) throws Exception;
}
