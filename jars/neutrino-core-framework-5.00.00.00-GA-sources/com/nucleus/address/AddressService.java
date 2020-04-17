package com.nucleus.address;

public interface AddressService {
	Address getAddressByAddressIdAndSource(Long addressId,String source);
}
