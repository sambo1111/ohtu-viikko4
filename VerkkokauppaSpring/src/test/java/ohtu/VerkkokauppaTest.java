/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ohtu;

/**
 *
 * @author hasasami
 */
import ohtu.verkkokauppa.Kauppa;
import ohtu.verkkokauppa.Pankki;
import ohtu.verkkokauppa.Tuote;
import ohtu.verkkokauppa.Varasto;
import ohtu.verkkokauppa.Viitegeneraattori;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class VerkkokauppaTest {

    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        // luodaan ensin mock-oliot
        Pankki pankki = mock(Pankki.class);

        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);

        Varasto varasto = mock(Varasto.class);
        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        Kauppa k = new Kauppa(varasto, pankki, viite);

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto("pekka", 42 , "12345", "33333-44455", 5);
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }
    
    @Test
    public void tilisiirtoToimiiKahdellaEriOstoksella() {
        Pankki pankki = mock(Pankki.class);
        
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(55);
        
        Varasto varasto = mock(Varasto.class);
        
        when(varasto.saldo(1)).thenReturn(5);
        when(varasto.saldo(2)).thenReturn(6);
        
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "kalja", 7));
        
        Kauppa k = new Kauppa(varasto, pankki, viite);
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("jorma", "111-111");
        
        verify(pankki).tilisiirto("jorma", 55, "111-111", "33333-44455", 12);
    }
    
    @Test
    public void tilisiirtoToimiiKahdellaSamallaOstoksella() {
        Pankki pankki = mock(Pankki.class);
        
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(55);
        
        Varasto varasto = mock(Varasto.class);
        
        when(varasto.saldo(2)).thenReturn(6);
        
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "kalja", 7));
        
        Kauppa k = new Kauppa(varasto, pankki, viite);
        
        k.aloitaAsiointi();
        k.lisaaKoriin(2);
        k.lisaaKoriin(2);
        k.tilimaksu("jorma", "111-111");
        
        verify(pankki).tilisiirto("jorma", 55, "111-111", "33333-44455", 14);
    }
    
    @Test
    public void tilisiirtoToimiiKahdellaOstoksellaJoistaToinenOnLoppu() {
        Pankki pankki = mock(Pankki.class);
        
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(55);
        
        Varasto varasto = mock(Varasto.class);
        
        when(varasto.saldo(1)).thenReturn(6);
        when(varasto.saldo(2)).thenReturn(0);
        
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "kalja", 7));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "viina", 10));
        
        Kauppa k = new Kauppa(varasto, pankki, viite);
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(2);
        k.tilimaksu("jorma", "111-111");
        
        verify(pankki).tilisiirto("jorma", 55, "111-111", "33333-44455", 7);
    }
    
    @Test
    public void aloitaAsiointiMetodiNollaaEdellisenOstoksenTiedot () {
        Pankki pankki = mock(Pankki.class);
        
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(55);
        
        Varasto varasto = mock(Varasto.class);
        
        when(varasto.saldo(1)).thenReturn(6);
        when(varasto.saldo(2)).thenReturn(10);
        
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "kalja", 7));
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "viina", 10));
        
        Kauppa k = new Kauppa(varasto, pankki, viite);
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.lisaaKoriin(1);
        
        // Uusi asiointi, ostoskorin pitäisi nollautua
        k.aloitaAsiointi();
        k.lisaaKoriin(2);
        k.tilimaksu("jorma", "111-111");
        
        verify(pankki).tilisiirto("jorma", 55, "111-111", "33333-44455", 10);
    }
    
    @Test
    public void generoidaanUusiViiteJokaiseenMaksuun() {
        Pankki pankki = mock(Pankki.class);
        
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        
        Varasto varasto = mock(Varasto.class);
        
        when(varasto.saldo(1)).thenReturn(6);
        
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "kalja", 7));
        
        Kauppa k = new Kauppa(varasto, pankki, viite);
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("jorma", "111-111");
        
        verify(viite, times(1)).uusi();
        
        k.aloitaAsiointi();
        k.lisaaKoriin(1);
        k.tilimaksu("jorma", "111-111");
        
        verify(viite, times(2)).uusi();
    }
    
    @Test
    public void tuotteenVoiPoistaaKorista() {
        Pankki pankki = mock(Pankki.class);
        
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        when(viite.uusi()).thenReturn(55);
        
        Varasto varasto = mock(Varasto.class);
        
        when(varasto.saldo(1)).thenReturn(5);
        
        Tuote t = new Tuote(1, "kalja", 7);
        when(varasto.haeTuote(1)).thenReturn(t);
        
        Kauppa k = new Kauppa(varasto, pankki, viite);
        
        k.aloitaAsiointi();
        
        k.lisaaKoriin(1);
        
        k.poistaKorista(1);
        
        verify(varasto, times(1)).palautaVarastoon(t);
       
    }
}
