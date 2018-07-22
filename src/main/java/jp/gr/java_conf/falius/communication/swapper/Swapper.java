package jp.gr.java_conf.falius.communication.swapper;

import jp.gr.java_conf.falius.communication.core.SwapClient;
import jp.gr.java_conf.falius.communication.listener.OnReceiveListener;
import jp.gr.java_conf.falius.communication.rcvdata.ReceiveData;
import jp.gr.java_conf.falius.communication.senddata.SendData;

/**
 * <p>
 * 受信内容を受け取り、送信内容を格納した{@link SendData}オブジェクトを作成するクラスのインターフェースです。
 * データの受信と送信を繋ぎ、送信データのファクトリとしての役割を担います。
 *
 * <p>
 * Swapperは一つの接続に対して一つのオブジェクトがあてられます。
 *
 *<p>
 * 受信内容をもとに送信内容を決定するために{@link ReceiveData}オブジェクトを引数に受け取りますが、
 * クライアントにおいては最初の一度はreceiveDataにnullが入り、最後の受信内容はswap()メソッドで
 * 受け取れないなど不便なものとなっています。そのため、Swapperは純粋にSendDataオブジェクトを生成するための
 * オブジェクトとし、受信内容の処理は一度の送受信なら{@link SwapClient#start}メソッドなどの戻り値、複数の送受信を行うなら
 * {@link OnReceiveListener#onReceive}の引数に渡されるReceiveDataオブジェクトを利用するのが確実です。
 *
 *<p>
 * サーバーとクライアントが同じ回数だけswapメソッドが呼ばれるように{@link doContinue}メソッドがfalseを返すように調整する
 * ことで、お互い自然に通信を終えることができます。
 *
 *<p>
 * 具象クラスは、一度の送受信であれば{@link OnceSwapper}、送受信を繰り返す場合は{@link RepeatSwapper}などが
 * 利用できます。
 *
 * @author "ymiyauchi"
 * @since 1.0
 *
 */
public interface Swapper {

    /**
     * <p>
     * 受信データが格納されたReceiveDataからデータを取得し、
     * 送信するデータを格納したSendDataオブジェクトを作成して戻り値としてください
     *
     *<p>
     * swapメソッドは送信の直前に実行されます。そのため、受信直後に実行され
     * るOnReceiveListener#onReceiveメソッドにて消費された受信データは
     * このメソッドに渡されるReceiveDataオブジェクトには入っていません。
     *
     *<p>
     * nullを返すと通信を強制的に終了します。
     * この場合、クライアントかサーバーかに関係なく送信直前に接続を切断します。
     * 通信相手は受信に失敗したことを検知して接続を切ることになります。
     *
     *<p>
     * クライアントに限り、最初の一度だけreceiveDataにnullが渡されますので注意してください。
     *
     * @since 1.0
     *
     */
    SendData swap(String remoteAddress, ReceiveData receiveData) throws Exception;

    /**
     * 通信を続けるかどうかを返すメソッドです。
     * このメソッドはクライアントでは受信直後、サーバーでは送信直後に呼ばれ、falseとなった時点で接続を切断します。
     * @return 通信を続けるかどうか
     * @since 1.0
     */
    boolean doContinue();
}
