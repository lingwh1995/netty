package org.openatom.netty.protocol.message.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.openatom.netty.protocol.message.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 支持多线程的的消息解码器(只要接收的ByteBuf消息是完整的,则可以视为这个消息解码器是无状态的,就可以在多线程环境中使用)
 * 注意: 必须要和LengthFieldBasedFrameDecoder一起使用,确保接收的ByteBuf消息是完整的,才能在多线程环境中使用
 * 自定义协议要素
 *  1.魔数:      用来在第一时间判断是否无效数据包
 *  2.版本号:    可以支持协议版本的升级
 *  3.序列化算法: 消息正文采用的序列化算法,如 json、jdk、protobuf
 *  4.指令(消息)类型:   是登录、注册、单聊、群聊......(与实际业务相关)
 *  5.请求序列号:   为双工通讯提供了异步能力
 *  6.消息正文长度
 *  7.消息正文
 */

@Slf4j
@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {

    /**
     * 对消息进行编码
     * @param channelHandlerContext
     * @param message
     * @param outList
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, List<Object> outList) throws Exception {
        ByteBuf out = channelHandlerContext.alloc().buffer();
        // 1.写出魔数(4字节)
        out.writeBytes(new byte[]{1,2,3,4});
        // 2.消息的版本(1字节)
        out.writeByte(1);
        // 3.序列化算法,0:使用jdk进行序列化 1:json(1字节)
        out.writeByte(0);
        // 4.的指令(消息)类型,父类中已经定义了消息类型,这里直接用就可以了(1字节)
        out.writeByte(message.LoginRequestMessage);
        // 5.请求序列号,父类中已经定义了请求序号,这里直接用就可以了(4字节)
        out.writeInt(message.getSequenceId());

        //再写出一个无意义的字节用于对齐,因为消息长度为15,再写出这个字节后,消息的长度为16(2的4次方)
        out.writeByte(0xff);

        //获取消息内容的字节数组(长度为四个字节)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(message);
        byte[] bytes = bos.toByteArray();

        // 6.消息正文长度(4字节)
        out.writeInt(bytes.length);
        // 7.消息正文
        out.writeBytes(bytes);

        // 传递给下一个Handler
        outList.add(message);
    }

    /**
     * 对消息进行解码
     * @param channelHandlerContext
     * @param in
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        // 1.读取魔数(4字节)
        int magicNum = in.readInt();
        // 2.读取消息版本(1字节)
        byte version = in.readByte();
        // 3.读取序列化类型(1字节)
        byte serializerType = in.readByte();
        // 4.读取消息类型(1字节)
        byte messageType = in.readByte();
        // 5.读取请求序列号
        int sequenceId = in.readInt();
        in.readByte();
        // 6.读取消息长度
        int messageLength = in.readInt();
        // 7.读取消息正文
        byte[] bytes = new byte[messageLength];
        in.readBytes(bytes,0,messageLength);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Message message = (Message)ois.readObject();
        log.debug("{},{},{},{},{},{}",magicNum,version,serializerType,messageType,sequenceId,messageLength);
        log.debug("{}",message);
        //给下一个handler用
        list.add(message);
    }
}
