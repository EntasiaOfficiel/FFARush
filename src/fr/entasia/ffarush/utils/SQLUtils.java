package fr.entasia.ffarush.utils;

import fr.entasia.ffarush.Main;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLUtils {

	public static PreparedStatement getSQLSaveObj(FFAPlayer ffp) throws SQLException {
		PreparedStatement ps = Main.sqlConnection.connection.prepareStatement(
				"UPDATE entagames set ffa_kills=?, ffa_deaths=?, ffa_blocks=?, ffa_cblock=?, ffa_inv=? where uuid=?");
		ps.setInt(1, ffp.kills);
		ps.setInt(2, ffp.deaths);
		ps.setByte(3, ffp.blocks);
		ps.setByte(4, ffp.block);
		if(ffp.inv==null)ps.setString(5, null);
		else{
			int a = 0;
			for(int i=0;i<ffp.inv.length;i++){
				if(ffp.inv[i]==40){
					a+=(9<<i*4);
				}
				else a+=(ffp.inv[i]<<i*4);
			}
			ps.setInt(5, a);
		}
		ps.setString(6, ffp.p.getUniqueId().toString());
		return ps;
	}

	private static byte[] bigIntToByteArray( final int i ) {
		BigInteger bigInt = BigInteger.valueOf(i);
		return bigInt.toByteArray();
	}

//    public static void main(String[] fa){
//        byte[] inv = new byte[6];
//
//        byte[] ex = bigIntToByteArray(3428880);
//
//        int a=0;
//        for(byte i : ex){
//            a=(a<<8)+i;
//            System.out.println(Integer.toBinaryString(a));
//        }
//        for(int i=0;i<inv.length;i++){
//            inv[i] = (byte) ((15<<i*4 & a)>>i*4); // 15 = 1111 , on récupère les bits 4 par 4
//            if(inv[i]==9) inv[i] = 40;
//            System.out.println("Item ID:"+i+" en slot "+inv[i]);
//        }
//    }
}
