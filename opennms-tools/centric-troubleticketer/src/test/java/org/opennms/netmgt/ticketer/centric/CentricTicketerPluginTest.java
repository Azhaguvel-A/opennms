package org.opennms.netmgt.ticketer.centric;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.opennms.netmgt.ticketd.Ticket;

public class CentricTicketerPluginTest extends TestCase {
    
    CentricTicketerPlugin m_ticketer;
    
    
    
    @Override
    protected void setUp() throws Exception {
        
        System.setProperty("opennms.home", "src"+File.separatorChar+"test"+File.separatorChar+"opennms-home");
        
        m_ticketer = new CentricTicketerPlugin();
    }

    public void testSave() {
        
        Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary("This is the summary");
        ticket.setDetails("These are the details");
        
        m_ticketer.saveOrUpdate(ticket);
        
        assertNotNull(ticket.getId());
        
        Ticket newTicket = m_ticketer.get(ticket.getId());
        
        assertNotNull(newTicket);
        assertTicketEquals(ticket, newTicket);
        
    }
    
    public void testUpdate() throws Exception {
        
        String summary = "A Ticket at "+new Date();

        Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary(summary);
        ticket.setDetails("Ticket details for ticket: "+new Date());
        
        m_ticketer.saveOrUpdate(ticket);
        
        assertNotNull(ticket.getId());
        

        Ticket newTicket = m_ticketer.get(ticket.getId());
        
        assertNotNull(newTicket);
        assertTicketEquals(ticket, newTicket);
        
        
        newTicket.setState(Ticket.State.CANCELLED);
        newTicket.setDetails("These details have changed");
        
        System.err.println("TicketId = "+newTicket.getId());
        
        m_ticketer.saveOrUpdate(newTicket);
        
        Thread.sleep(500);
        
        Ticket newerTicket = m_ticketer.get(newTicket.getId());
        
        assertTicketEquals(newTicket, newerTicket);
    }



    private void assertTicketEquals(Ticket ticket, Ticket newTicket) {
        assertEquals(ticket.getId(), newTicket.getId());
        assertEquals(ticket.getState(), newTicket.getState());
        assertEquals(ticket.getSummary(), newTicket.getSummary());
        
        //TODO: Implement this later when we need 2 way retrievel of commments/details
        //assertEquals(ticket.getDetails(), newTicket.getDetails());
    }
    
    public void testGet() {
        
    	//This may need to be changed ;-)
        String ticketId = "92";
		Ticket newTicket = m_ticketer.get(ticketId);
        
        assertNotNull(newTicket);
        assertEquals(ticketId, newTicket.getId());
        assertTrue(newTicket.getSummary().startsWith("A Ticket at"));
        
        //TODO: Implement this later when we need 2 way retrievel of commments/details
        //assertEquals("These are the details", newTicket.getDetails());
        
    }
    
    
    
    

}
