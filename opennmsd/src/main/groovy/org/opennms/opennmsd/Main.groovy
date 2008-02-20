package org.opennms.opennmsd;

import org.apache.log4j.Logger;

public class Main {
    
    private static Logger log = Logger.getLogger(Main.class);

    static {
        log.info("Loaded "+Main.class.getName());
        try { Thread.sleep(20000); } catch (Exception e) {}
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            
            log.info("Starting opennmsd: ${args}");

            if (args.length < 1) {
                throw new IllegalArgumentException("Configuration files must be specified");
            }
            
            OpenNMSDaemon daemon = new OpenNMSDaemon();
            
            DefaultConfiguration config = new DefaultConfiguration();
            config.setConfigFile(new File(args[0]));
            config.load();
            daemon.setConfiguration(config);

            DefaultEventForwarder forwarder = new DefaultEventForwarder();
            forwarder.setOpenNmsHost(config.getOpenNmsHost());
            forwarder.setPort(config.getPort())
            daemon.setEventForwarder(forwarder);
            
            TrapdConfiguration trapdConf = new TrapdConfiguration();
            trapdConf.setTrapConf(new File(args[1]));
            trapdConf.load();

            DefaultNNMEventFactory eventFactory = new DefaultNNMEventFactory();
            eventFactory.setEventConfiguation(trapdConf);
            daemon.setEventFactory(eventFactory);
            
            daemon.execute();

        } catch(Exception e) {
            log.error("Exception executing opennmsd", e);
            System.exit(27);
        }
        
        System.exit(0);
        
    }

}
