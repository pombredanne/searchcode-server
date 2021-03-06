#!/usr/local/bin/python
# coding: utf-8

import unittest
import urllib2
import json
import sys
import re
import os
import random
import string

'''Variety of simple tests to check that nothing is obviously broken'''
class TestIntegration(unittest.TestCase):
    def getData(self, url):
        data = urllib2.urlopen(url)
        self.assertEqual(200, data.getcode())
        data = data.read()
        return data

    def getRandomLetters(self, count):
        return ''.join(random.choice(string.letters) for i in xrange(count))

    def testMainPage(self):
        data = self.getData("http://%s/" % (host))
        self.assertTrue('Searching across' in data)

    def testDocumentationPage(self):
        data = self.getData("http://%s/documentation/" % (host))
        self.assertTrue('<h2>Documentation</h2>' in data)

    def testLoginPage(self):
        data = self.getData("http://%s/login/" % (host))
        self.assertTrue('Enter Password' in data)

    def testAdminRedirect(self):
        data = self.getData("http://%s/admin/" % (host))
        self.assertTrue('Enter Password' in data)

    def testAdminBulkRedirect(self):
        data = self.getData("http://%s/admin/bulk/" % (host))
        self.assertTrue('Enter Password' in data)

    def testAdminSettingsRedirect(self):
        data = self.getData("http://%s/admin/settings/" % (host))
        self.assertTrue('Enter Password' in data)

    def testMinified(self):
        data = self.getData("http://%s/?q=test" % (host))
        self.assertTrue('<script src="/js/script.min.js"></script>' in data)

    def testJsonLoads(self):
        data = self.getData("http://%s/api/codesearch/?q=test&p=0" % (host))
        data = json.loads(data)
        self.assertTrue('totalHits' in data)

    def testSearchJsPreload(self):
        data = self.getData("http://%s/?q=test" % (host))
        self.assertTrue('var preload = {' in data)

    def testSearch(self):
        data = self.getData("http://%s/html/?q=test" % (host))
        self.assertTrue('refine search' in data)

    def testDepracatedCodeResults(self):
        for x in xrange(10):
            url = "http://%s/codesearch/view/%s" % (host, x)
            data = self.getData(url)
            self.assertTrue('MD5 Hash' in data)

    def testCodeResults(self):
        url = "http://%s/file/zeroclickinfo-fathead/lib/fathead/java/test_parse.py" % (host)
        data = self.getData(url)
        #self.assertTrue('MD5 Hash' in data)

    def testNoSearch(self):
        url = "http://%s/?q=&p=0" % (host)
        data = self.getData(url)
        self.assertTrue('Searching across' in data)

    def testNoSearchHtml(self):
        url = "http://%s/html/?q=&p=0" % (host)
        data = self.getData(url)

    def testNoSearchJson(self):
        url = "http://%s/api/codesearch/?q=&p=0" % (host)
        data = self.getData(url)

    def testSearchLoad(self):
        for x in xrange(1000):
            url = "http://%s/html/?q=%s" % (host, self.getRandomLetters(10))
            data = self.getData(url)
            self.assertTrue('refine search' in data)

    def testFuzzyBadData(self):
        self.getData("http://%s/html/?q=test&p=100" % (host))
        self.getData("http://%s/html/?q=test&p=a" % (host))
        self.getData("http://%s/html/?&p=a" % (host))
        self.getData("http://%s/html/?q=test&p=1asds" % (host))
        self.getData("http://%s/html/?q=test&p=1&repo=test&lan=test" % (host))

        for x in xrange(1000):
            url = "http://%s/html/?%s=%s&%s=%s" % (
                host, 
                self.getRandomLetters(1), 
                self.getRandomLetters(10), 
                self.getRandomLetters(1), 
                self.getRandomLetters(10))
            self.getData(url)

        for x in xrange(1000):
            self.getData("http://%s/html/?q=%s&repo=%s&lan=%s" % (
                host, 
                self.getRandomLetters(10), 
                self.getRandomLetters(10),
                self.getRandomLetters(10)))

    def testRepoPaths(self):
        '''
        This needs a reasonable amount of data to work correctly
        it is also slow, so disabled it most of the time
        borked if looking up binary file
        '''

        binary = 'jpg|jpeg|gif|jar|png|.gitignore|webp'.split('|')

        fileschecked = 0
        filesbad = 0

        for dirName, subdirList, fileList in os.walk('../../repo/'):
            if '/.git/' not in dirName and dirName.endswith('/.git') == False:

                for f in fileList:
                    
                    found = [x for x in binary if x in f]

                    if len(found) == 0:
                        searchcodepath = '%s/%s' % (dirName.replace('../../repo/', ''), f)

                        url = "http://%s/file/%s" % (host, searchcodepath)
                        
                        fileschecked = fileschecked + 1

                        try:    
                            data = urllib2.urlopen(url)
                        except:
                            print url
                        
                        self.assertEqual(200, data.getcode())
                        data = data.read()

                        if 'Was not able to find this code file' in data:
                            print url
                            filesbad = filesbad + 1

        # Less than 1% should fail
        self.assertTrue( filesbad < (fileschecked / 100))



if __name__ == "__main__":
    host = "localhost:8080"
    unittest.main()
