import React, { useState } from 'react';
import baseRequest from "../network/baseRequest";
import { Link, useNavigate } from 'react-router-dom';
import "./styles/ResetPassword.css";

const ResetPassword = () =>{
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: ''
      });
      const navigate = useNavigate();
      const [errors, setErrors] = useState({});
    
      const validateForm = () => {
        const newErrors = {};
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_-])[A-Za-z\d@$!%*#?&_-]+$/;
    
        if (!emailRegex.test(formData.email)) {
          newErrors.email = 'Inccorect email format';
        }
        if (formData.email.length > 40) {
          newErrors.email = 'Email is too big (max size is: 40 symbols)';
        }
        if (formData.password.length < 9 || formData.password.length > 30) {
          newErrors.password = 'Password must contain from 9 to 30 characters';
        }
        if (!passwordRegex.test(formData.password)) {
          newErrors.password = 'Password must contain at least one uppercase letter, one lowercase letter, one number and one special character';
        }
        if (formData.password !== formData.confirmPassword) {
          newErrors.confirmPassword = 'Passwords do not match';
        }
    
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
      };
    
      const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
      };
    
      const handleSubmit = async (e) => {
        e.preventDefault();
        if (validateForm()) {
          try {
            const response = await baseRequest.post('/api/user/change-password', formData, {
              headers: {
                'Content-Type': 'application/json'
              }
            });
            if (response.status === 202) {
              navigate("/summit-password-change")
            }
          } catch (error) {
            if (error.response && error.response.data) {
              setErrors(error.response.data);
            } else {
              console.error('Change password error:', error);
            }
          }
        }
      };
    
      return (
        <div id='respass'>
          <h2>Reset Password</h2>
          <form onSubmit={handleSubmit}>
            <div>
              <label htmlFor="email">Email:</label>
              <input
                type="email"
                id="email"
                name="email"
                className='txt1'
                value={formData.email}
                onChange={handleChange}
                required
              />
              {errors.email && <p>{errors.email}</p>}
            </div>
            <div>
              <label htmlFor="password">Password:</label>
              <input
                type="password"
                id="password"
                name="password"
                className='txt1'
                value={formData.password}
                onChange={handleChange}
                required
              />
              {errors.password && <p>{errors.password}</p>}
            </div>
            <div>
              <label htmlFor="confirmPassword">Repeat Password:</label>
              <input
              className='txt1'
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              />
              {errors.confirmPassword && <p>{errors.confirmPassword}</p>}
            </div>
            <button type="submit" id='sub'>Reset password</button>
          </form>
          <p>You have an account? <Link to="/login">Login</Link></p>
          <p>You do not have account? <Link to="/register">Signup</Link></p>
        </div>
      );
};

export default ResetPassword;