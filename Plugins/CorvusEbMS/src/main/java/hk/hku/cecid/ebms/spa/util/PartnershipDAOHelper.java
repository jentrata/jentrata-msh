package hk.hku.cecid.ebms.spa.util;

import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.dao.PartnershipDAO;
import hk.hku.cecid.ebms.spa.dao.PartnershipDVO;
import hk.hku.cecid.piazza.commons.dao.DAOException;

public class PartnershipDAOHelper {
    
    public static boolean isChannelRegistered(String cpaId, String service, String action) throws DAOException {
        
        PartnershipDAO partnershipDAO = (PartnershipDAO) EbmsProcessor.core.dao
            .createDAO(PartnershipDAO.class);
        
        PartnershipDVO partnershipDVO = (PartnershipDVO) partnershipDAO.createDVO();
        partnershipDVO.setCpaId(cpaId);
        partnershipDVO.setService(service);
        partnershipDVO.setAction(action);
        
        return partnershipDAO.findPartnershipByCPA(partnershipDVO);
    }

}
