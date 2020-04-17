package com.nucleus.address;

import javax.inject.Inject;
import javax.inject.Named;

@Named("addressServiceCore")
public class AddressServiceImpl implements AddressService{

    @Inject
    @Named("addressService")
    private AddressTagService addressService;
    
    /*Default implementation for Core*/
	@Override
	public Address getAddressByAddressIdAndSource(Long addressId,
			String source) {
		
		return null;
	}

}
