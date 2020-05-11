package com.gupaoedu;

import com.gupaoedu.domain.Shop;
import com.gupaoedu.mapper.ShopMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Description
 * @Author chenzk27336
 * @Date 2020/5/11 15:39
 **/
public class ShopTest {
    private ShopMapper mapper ;
    private SqlSession session ;

    @Before
    public void init(){
        System.out.println("init......");
        try {
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            session = sqlSessionFactory.openSession(); // ExecutorType.BATCH
            mapper = session.getMapper(ShopMapper.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // session.close();
        }
    }
    @Test
    public void testQueryById(){
        Shop shop = mapper.selectShopById("1");
        System.out.println(shop.toString());
    }

}
