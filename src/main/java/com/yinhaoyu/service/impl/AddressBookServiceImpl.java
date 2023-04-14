package com.yinhaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinhaoyu.entity.AddressBook;
import com.yinhaoyu.mapper.AddressBookMapper;
import com.yinhaoyu.service.AddressBookService;
import org.springframework.stereotype.Service;

/**
 * @author Vastness
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}