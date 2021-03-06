/**
 * MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT
 *
 * Copyright (c) 2010-11 The Trustees of Columbia University in the City of
 * New York and Grameen Foundation USA.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Grameen Foundation USA, Columbia University, or
 * their respective contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA, COLUMBIA UNIVERSITY
 * AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION
 * USA, COLUMBIA UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.motechproject.server.omod.web.model;

import org.apache.commons.lang.StringUtils;
import org.motechproject.server.model.MessageLanguage;
import org.motechproject.server.model.PreferredLocation;
import org.motechproject.server.model.ghana.Community;
import org.motechproject.server.model.ghana.Country;
import org.motechproject.server.model.ghana.Facility;
import org.motechproject.server.model.ghana.FacilityComparator;
import org.motechproject.server.service.ContextService;
import org.motechproject.server.service.MotechService;
import org.openmrs.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import java.util.*;

public class JSONLocationSerializer {

    private ContextService contextService;
    private Comparator<Community> communityNameComparator;

    public JSONLocationSerializer() {
        communityNameComparator = new Comparator<Community>() {
            public int compare(Community c1, Community c2) {
                return c1.getName().compareTo(c2.getName());
            }
        };
    }

    @Autowired
    public void setContextService(ContextService contextService) {
        this.contextService = contextService;
    }

    public void populateJavascriptMaps(ModelMap model) {
        Map<String, TreeSet<String>> regionMap = new HashMap<String, TreeSet<String>>();
        Map<String, TreeSet<Community>> districtMap = new HashMap<String, TreeSet<Community>>();

        MotechService motechService = contextService.getMotechService();
        List<Facility> facilities = motechService.getAllFacilities();
        List<MessageLanguage> languages = motechService.getAllLanguages();

        for (Facility facility : facilities) {
            Location location = facility.getLocation();
            if (location != null) {
                String region = location.getRegion();
                String district = location.getCountyDistrict();
                TreeSet<String> districts = regionMap.get(region);
                if (districts == null) {
                    districts = new TreeSet<String>();
                }
                if (StringUtils.isNotEmpty(district)) districts.add(district);
                regionMap.put(region, districts);
                TreeSet<Community> communities = districtMap.get(district);
                if (communities == null) {
                    communities = new TreeSet<Community>(communityNameComparator);
                }
                communities.addAll(facility.getCommunities());
                districtMap.put(district, communities);
            }
        }
        FacilityComparator facilityComparator = new FacilityComparator();
        Collections.sort(facilities, facilityComparator);

        model.addAttribute("languages", languages);
        model.addAttribute("regionMap", regionMap);
        model.addAttribute("districtMap", districtMap);
        model.addAttribute("facilities", facilities);
        model.addAttribute("country", new Country("Ghana").withFacilities(facilities));

    }

    public void populateJavascriptMaps(ModelMap model, PreferredLocation preferredLocation) {
        populateJavascriptMaps(model);
        model.addAttribute("selectedLocation", preferredLocation);
    }

    protected boolean regionIsUpperEast(WebPatient webPatient) {
        return webPatient.hasRegion("Upper East");
    }

    public List<String> getAllRegions() {
        return contextService.getMotechService().getAllRegions();
    }

    public List<String> getAllDistricts() {
        return contextService.getMotechService().getAllDistricts();
    }

    public List<Community> getAllCommunities(boolean includeRetired) {
        return contextService.getMotechService().getAllCommunities(includeRetired);
    }
}
