const request = require('supertest');
const app = require('../app');

describe('DigitalBalance Backend API Tests', () => {

  test('GET /api/health returns 200 OK and ONLINE status', async () => {
    const res = await request(app).get('/api/health');
    expect(res.statusCode).toBe(200);
    expect(res.body.status).toBe('ONLINE');
  });

  test('GET /api/usage without auth header returns 401 Unauthorized', async () => {
    const res = await request(app).get('/api/usage');
    expect(res.statusCode).toBe(401);
    expect(res.body.success).toBe(false);
  });

  test('POST /api/usage/sync with dev token saves records', async () => {
    const res = await request(app)
      .post('/api/usage/sync')
      .set('Authorization', 'Bearer demo_dev_token_testuser')
      .send({
        usageRecords: [
          {
            date: '2026-10-05',
            packageName: 'com.instagram.android',
            appName: 'Instagram',
            category: 'SOCIAL',
            foregroundTimeMs: 3600000,
            sessionCount: 12,
            isSocialMedia: true
          }
        ]
      });

    expect(res.statusCode).toBe(200);
    expect(res.body.success).toBe(true);
  });

  test('POST /api/usage/sync with invalid empty body returns 400 Bad Request', async () => {
    const res = await request(app)
      .post('/api/usage/sync')
      .set('Authorization', 'Bearer demo_dev_token_testuser')
      .send({ usageRecords: [] });

    expect(res.statusCode).toBe(400);
    expect(res.body.success).toBe(false);
  });

});
