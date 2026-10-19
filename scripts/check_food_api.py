"""Bounded live smoke check; does not send personal diary data."""
import json
import urllib.request
import urllib.parse
from pathlib import Path

URLS = [
    'https://world.openfoodfacts.org/api/v2/product/3017620422003.json',
    'https://search.openfoodfacts.org/search?' + urllib.parse.urlencode({
        'q': 'Milbona AND countries_tags:"en:serbia"', 'page_size': 5, 'langs': 'ru:sr:en', 'fields': 'code,product_name,nutriments'}),
    'https://search.openfoodfacts.org/search?' + urllib.parse.urlencode({
        'q': 'countries_tags:"en:serbia" AND stores:"lidl"', 'page_size': 5, 'langs': 'ru:sr:en', 'fields': 'code,product_name,nutriments'}),
]
for index, url in enumerate(URLS):
    try:
        request = urllib.request.Request(url, headers={'User-Agent': 'DietHelper/1.1 (Android; personal food diary)'})
        with urllib.request.urlopen(request, timeout=20) as response:
            body = response.read()
            Path(f'.local/off-live-{index}.json').write_bytes(body)
            data = json.loads(body)
            print(index, response.status, len(body), data.get('count', data.get('status')), flush=True)
    except Exception as error:
        print(index, str(error), flush=True)
